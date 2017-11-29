package me.akrivos.nana
package api

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.RouteResult.Complete
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import me.akrivos.nana.model._
import me.akrivos.nana.repository._
import me.akrivos.nana.service.LabResultService

class Api(
  labResultCodeRepo: LabResultCodeRepository,
  labResultRepo: LabResultRepository,
  patientRepo: PatientRepository,
  labResultService: LabResultService,
  realm: String,
  authenticator: Credentials => Option[UserInfo]
) extends Directives with SecurityDirectives with FailFastCirceSupport {

  private def logRequestResponseDetails(userInfo: UserInfo): Directive0 =
    (extractClientIP & extractRequestContext & extractLog).tflatMap {
      case (ip, ctx, log) =>
        val timestamp = System.currentTimeMillis
        val request   = ctx.request
        (extractSettings & extractParserSettings & extractExecutionContext).tflatMap {
          case (rs, ps, ec) =>
            mapRouteResultFuture { result =>
              val sealedRoute = Route.seal(ctx => result)(rs, ps)
              sealedRoute(ctx).onComplete {
                case scala.util.Success(Complete(response)) =>
                  val duration = System.currentTimeMillis - timestamp
                  val message = "[%s - %s] %s %s returned %d in %dms".format(
                    ip,
                    userInfo.username,
                    request.method.value,
                    request.uri.path,
                    response.status.intValue(),
                    duration
                  )
                  response.status match {
                    case NotFound       => log.info(message)
                    case ServerError(_) => log.error(message)
                    case ClientError(_) => log.warning(message)
                    case _              => log.info(message)
                  }
                case _ =>
              }(ec)
              result
            }
        }
    }

  val route: Route = {
    authenticateOAuth2(realm, authenticator) { userInfo =>
      logRequestResponseDetails(userInfo) {
        pathPrefix("patients") {
          pathEndOrSingleSlash {
            get {
              parameter('withResults ? false) { withResults =>
                if (withResults) {
                  complete {
                    PatientsResults(
                      patientRepo.list.flatMap { patient =>
                        labResultService.getResultsForPatientId(patient.id)
                      }
                    )
                  }
                } else {
                  complete {
                    patientRepo.list
                  }
                }
              }
            } ~
            post {
              entity(as[RawPatient]) { entity =>
                complete {
                  if (patientRepo.insert(entity)) Created else Conflict
                }
              }
            }
          } ~
          path(JavaUUID) { id =>
            get {
              complete {
                patientRepo.findById(id) match {
                  case Some(patient) => patient
                  case None          => NotFound
                }
              }
            } ~
            put {
              entity(as[UpdatePatient]) { entity =>
                complete {
                  if (patientRepo.update(id, entity)) OK else NotFound
                }
              }
            } ~
            delete {
              complete {
                if (patientRepo.delete(id)) OK else NotFound
              }
            }
          }
        } ~
        pathPrefix("codes") {
          pathEndOrSingleSlash {
            get {
              complete {
                labResultCodeRepo.list
              }
            } ~
            post {
              entity(as[ResultCode]) { entity =>
                complete {
                  if (labResultCodeRepo.insert(entity)) Created else Conflict
                }
              }
            }
          } ~
          path(Segment) { key =>
            get {
              complete {
                labResultCodeRepo.findByKey(key) match {
                  case Some(code) => code
                  case None       => NotFound
                }
              }
            } ~
            put {
              entity(as[UpdateResultCode]) { entity =>
                complete {
                  if (labResultCodeRepo.update(key, entity)) OK else NotFound
                }
              }
            } ~
            delete {
              complete {
                if (labResultCodeRepo.delete(key)) OK else NotFound
              }
            }
          }
        } ~
        pathPrefix("results") {
          pathEndOrSingleSlash {
            get {
              complete {
                labResultRepo.list
              }
            } ~
              post {
                entity(as[RawLabResult]) { entity =>
                  complete {
                    if (labResultRepo.insert(entity)) Created else Conflict
                  }
                }
              }
          } ~
          path(Segment) { id =>
            get {
              complete {
                labResultRepo.findById(id) match {
                  case Some(result) => result
                  case None         => NotFound
                }
              }
            } ~
            put {
              entity(as[UpdateLabResult]) { entity =>
                complete {
                  if (labResultRepo.update(id, entity)) OK else NotFound
                }
              }
            } ~
            delete {
              complete {
                if (labResultRepo.delete(id)) OK else NotFound
              }
            }
          }
        }
      }
    }
  }

}
