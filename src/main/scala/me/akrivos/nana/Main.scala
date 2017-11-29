package me.akrivos.nana

import java.io._
import java.net.URL
import java.security.interfaces.RSAPublicKey

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.directives.Credentials
import akka.stream.ActorMaterializer
import com.auth0.jwk.{GuavaCachedJwkProvider, UrlJwkProvider}
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._
import io.circe.syntax._
import me.akrivos.nana.api.Api
import me.akrivos.nana.model._
import me.akrivos.nana.repository._
import me.akrivos.nana.service.LabResultService

import scala.util.{Success, Try}

object Main extends App with StrictLogging {

  val labResultCodeRepo = new LabResultCodeRepository(
    getClass.getResource("/labresults-codes.csv")
  )
  val labResultRepo = new LabResultRepository(
    getClass.getResource("/labresults.csv")
  )
  val patientRepo = new PatientRepository(
    getClass.getResource("/patients.json")
  )

  val labResultService = new LabResultService(
    labResultCodeRepo,
    labResultRepo,
    patientRepo
  )

  val patientsResults = PatientsResults(
    patientRepo.list.flatMap { patient =>
      labResultService.getResultsForPatientId(patient.id)
    }
  )

  // write output.json
  val json = patientsResults.asJson.spaces2
  val pw   = new PrintWriter(new File("output.json"))
  pw.write(json)
  pw.close()

  // start API
  implicit val system = ActorSystem("api")
  implicit val mat    = ActorMaterializer()
  implicit val ec     = system.dispatcher

  val realm    = "interview"
  val jwkUrl   = new URL(s"https://auth.healthforge.io/auth/realms/$realm/protocol/openid-connect/certs")
  val provider = new GuavaCachedJwkProvider(new UrlJwkProvider(jwkUrl))
  val issuer   = s"https://auth.healthforge.io/auth/realms/$realm"
  val authenticator: Credentials => Option[UserInfo] = {
    case Credentials.Provided(token) =>
      Try {
        val kid       = JWT.decode(token).getKeyId
        val jwk       = provider.get(kid)
        val publicKey = jwk.getPublicKey.asInstanceOf[RSAPublicKey]
        val algo      = Algorithm.RSA256(publicKey, null)
        val verifier  = JWT.require(algo).withIssuer(issuer).build()
        val jwt       = verifier.verify(token)
        val userInfo = UserInfo(
          name = jwt.getClaim("name").asString(),
          username = jwt.getClaim("preferred_username").asString(),
          givenName = jwt.getClaim("given_name").asString(),
          familyName = jwt.getClaim("family_name").asString(),
          email = jwt.getClaim("email").asString()
        )
        userInfo
      }.toOption
    case _ =>
      None
  }

  val api = new Api(labResultCodeRepo, labResultRepo, patientRepo, labResultService, realm, authenticator)

  Http().bindAndHandle(api.route, "0.0.0.0", 8080).onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      logger.info(s"Service bound to ${address.getHostString}:${address.getPort}")
    case x =>
      logger.error(s"Unexpected HTTP bind result: $x")
      sys.exit(1)
  }
}
