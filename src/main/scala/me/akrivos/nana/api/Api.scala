package me.akrivos.nana
package api

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import me.akrivos.nana.model._
import me.akrivos.nana.repository._
import me.akrivos.nana.service.LabResultService

class Api(
  labResultCodeRepo: LabResultCodeRepository,
  labResultRepo: LabResultRepository,
  patientRepo: PatientRepository,
  labResultService: LabResultService
) extends Directives with FailFastCirceSupport {

  val route: Route = {
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
              case None => NotFound
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
              case None => NotFound
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
              case None => NotFound
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
