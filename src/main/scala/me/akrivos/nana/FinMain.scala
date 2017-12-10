package me.akrivos.nana

import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.finagle.param.Stats
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch.circe.jacksonSerializer._
import me.akrivos.nana.api.FinApi
import me.akrivos.nana.repository._
import me.akrivos.nana.service.LabResultService

object FinMain extends TwitterServer {

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

  val api = new FinApi(labResultCodeRepo, labResultRepo, patientRepo, labResultService)

  def main(): Unit = {
    val server: ListeningServer = Http.server
      .configured(Stats(statsReceiver))
      .serve(":8081", api.routes.toService)

    onExit { server.close(); () }

    Await.ready(adminHttpServer)
    ()
  }
}
