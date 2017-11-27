package me.akrivos.nana
package api

import akka.http.scaladsl.server._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import me.akrivos.nana.model._

class Api extends Directives with FailFastCirceSupport {

  val route: Route = {
    pathPrefix("patients") {
      pathEndOrSingleSlash {
        get {
          parameter('withResults ? false) { withResults =>
            complete(s"list-all-patients withResults=$withResults")
          }
        } ~
        post {
          entity(as[RawPatient]) { entity =>
            complete(s"new-patient entity=$entity")
          }
        }
      } ~
      path(JavaUUID) { id =>
        get {
          complete(s"get-patient id=$id")
        } ~
        put {
          entity(as[UpdatePatient]) { entity =>
            complete(s"update-patient id=$id entity=$entity")
          }
        } ~
        delete {
          complete(s"delete-patient id=$id")
        }
      }
    } ~
    pathPrefix("codes") {
      pathEndOrSingleSlash {
        get {
          complete("list-all-codes")
        } ~
        post {
          entity(as[ResultCode]) { entity =>
            complete(s"new-code entity=$entity")
          }
        }
      } ~
      path(Segment) { key =>
        get {
          complete(s"get-code key=$key")
        } ~
        put {
          entity(as[UpdateResultCode]) { entity =>
            complete(s"update-code key=$key entity=$entity")
          }
        } ~
        delete {
          complete(s"delete-code key=$key")
        }
      }
    } ~
    pathPrefix("results") {
      pathEndOrSingleSlash {
        get {
          complete("list-all-results")
        } ~
        post {
          entity(as[RawLabResult]) { entity =>
            complete(s"new-result entity=$entity")
          }
        }
      } ~
      path(Segment) { id =>
        get {
          complete(s"get-result id=$id")
        } ~
        put {
          entity(as[UpdateLabResult]) { entity =>
            complete(s"update-result id=$id entity=$entity")
          }
        } ~
        delete {
          complete(s"delete-result id=$id")
        }
      }
    }
  }
}
