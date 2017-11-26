package me.akrivos.nana

import java.io._

import io.circe.generic.auto._
import io.circe.syntax._
import me.akrivos.nana.model._
import me.akrivos.nana.repository._
import me.akrivos.nana.service.LabResultService

object Question1 extends App {

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
    patientRepo.toIterable.flatMap { patient =>
      labResultService.getResultsForPatientId(patient.id)
    }
  )

  val json = patientsResults.asJson.spaces2
  val pw   = new PrintWriter(new File("output.json"))
  pw.write(json)
  pw.close()
}
