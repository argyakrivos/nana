package me.akrivos.nana
package service

import java.util.UUID

import com.github.nscala_time.time.Imports._
import me.akrivos.nana.model._
import me.akrivos.nana.repository._
import org.scalatest.{FlatSpecLike, Matchers, OptionValues}

class LabResultServiceSpec extends FlatSpecLike with Matchers with OptionValues {

  class TestFixture {
    val labResultCodeRepo = new LabResultCodeRepository(getClass.getResource("/test-codes.csv"))
    val labResultRepo     = new LabResultRepository(getClass.getResource("/test-results.csv"))
    val patientRepo       = new PatientRepository(getClass.getResource("/test-patients.json"))
    val service           = new LabResultService(labResultCodeRepo, labResultRepo, patientRepo)
  }

  behavior of "LabResultService"

  it should "get results for patientId" in new TestFixture {
    val id = UUID.fromString("ccbf8ccb-12b9-4f33-b5a2-cb2d38039d3e")
    service.getResultsForPatientId(id).value shouldBe PatientResults(
      id = id,
      firstName = "Patient",
      lastName = "Alpha",
      dob = DateTime.parse("1980-01-10T00:00:00.000Z", DefaultDateTimeFormatter),
      lab_results = List(
        LabResult(
          timestamp = DateTime.parse("2014-08-09T00:00:00.000Z", DefaultDateTimeFormatter),
          profile = Profile(
            "BONE PROFILE",
            code = "BON"
          ),
          panel = List(
            PanelResult(
              code = "6768-6",
              label = "Alkaline phosphatase [Enzymatic activity/volume] in Serum or Plasma",
              value = "55",
              unit = "IU/L",
              lower = Some(35.0),
              upper = Some(104.0)
            ),
            PanelResult(
              code = "1751-7",
              label = "Albumin, Serum",
              value = "37",
              unit = "g/L",
              lower = Some(34.0),
              upper = Some(50.0)
            ),
            PanelResult(
              code = "17861-6",
              label = "Calcium, Serum",
              value = "2.18",
              unit = "mmol/L",
              lower = Some(2.2),
              upper = Some(2.6)
            ),
            PanelResult(
              code = "18281-6",
              label = "Corrected Calcium",
              value = "2.34",
              unit = "mmol/L",
              lower = Some(2.2),
              upper = Some(2.6)
            ),
            PanelResult(
              code = "2777-1",
              label = "Phosphorus (Inorganic), Serum",
              value = "1.29",
              unit = "mmol/L",
              lower = Some(0.87),
              upper = Some(1.45)
            )
          )
        )
      )
    )
  }
}
