package me.akrivos.nana

import java.io._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._
import io.circe.syntax._
import me.akrivos.nana.api.Api
import me.akrivos.nana.model._
import me.akrivos.nana.repository._
import me.akrivos.nana.service.LabResultService

import scala.util.Success

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
    labResultCodeRepo, labResultRepo, patientRepo
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
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  val api = new Api(labResultCodeRepo, labResultRepo, patientRepo, labResultService)

  Http().bindAndHandle(api.route, "0.0.0.0", 8080).onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      logger.info(s"Service bound to ${address.getHostString}:${address.getPort}")
    case x =>
      logger.error(s"Unexpected HTTP bind result: $x")
      sys.exit(1)
  }
}
