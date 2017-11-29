package me.akrivos.nana
package api

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.nscala_time.time.Imports._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import me.akrivos.nana.model._
import me.akrivos.nana.repository.{LabResultCodeRepository, LabResultRepository, PatientRepository}
import me.akrivos.nana.service.LabResultService
import org.scalatest.{FlatSpecLike, Matchers, OptionValues}

class ApiSpec extends FlatSpecLike with Matchers with ScalatestRouteTest with FailFastCirceSupport with OptionValues {

  trait TestFixture {
    val labResultCodeRepo = new LabResultCodeRepository(getClass.getResource("/test-codes.csv"))
    val labResultRepo     = new LabResultRepository(getClass.getResource("/test-results.csv"))
    val patientRepo       = new PatientRepository(getClass.getResource("/test-patients.json"))
    val labResultService  = new LabResultService(labResultCodeRepo, labResultRepo, patientRepo)
    val realm             = "interview"
    val auth              = (_: Credentials) => Some(UserInfo("User Test", "user", "User", "Test", "user@test.com"))
    val route             = new Api(labResultCodeRepo, labResultRepo, patientRepo, labResultService, realm, auth).route
  }

  behavior of "API"

  it should "list all patients without results" in new TestFixture {
    Get("/patients?withResults=false") ~> route ~> check {
      status shouldBe OK
      responseAs[List[RawPatient]] shouldBe List(
        RawPatient(
          id = UUID.fromString("ccbf8ccb-12b9-4f33-b5a2-cb2d38039d3e"),
          identifiers = List("41915278"),
          firstName = "Patient",
          lastName = "Alpha",
          dateOfBirth = DateTime.parse("1980-01-10T00:00:00.000Z")
        ),
        RawPatient(
          id = UUID.fromString("9341fce0-5864-4b8a-a850-1ec0689482b3"),
          identifiers = List("41723788"),
          firstName = "Patient",
          lastName = "Beta",
          dateOfBirth = DateTime.parse("1995-03-30T00:00:00.000Z")
        )
      )
    }
  }

  it should "list all patients with results" in new TestFixture {
    Get("/patients?withResults=true") ~> route ~> check {
      status shouldBe OK
      responseAs[PatientsResults] shouldBe PatientsResults(
        patients = List(
          PatientResults(
            id = UUID.fromString("ccbf8ccb-12b9-4f33-b5a2-cb2d38039d3e"),
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
          ),
          PatientResults(
            id = UUID.fromString("9341fce0-5864-4b8a-a850-1ec0689482b3"),
            firstName = "Patient",
            lastName = "Beta",
            dob = DateTime.parse("1995-03-30T00:00:00.000Z", DefaultDateTimeFormatter),
            lab_results = List.empty
          )
        )
      )
    }
  }

  it should "create a new patient" in new TestFixture {
    val id     = UUID.randomUUID()
    val entity = RawPatient(id, List("123"), "John", "Doe", DateTime.parse("2000-01-01T00:00:00.000Z"))
    Post("/patients", entity) ~> route ~> check {
      status shouldBe Created
      patientRepo.findById(id).value shouldBe entity
    }
  }

  it should "return Conflict if trying to create a patient with an existing id" in new TestFixture {
    val id     = UUID.fromString("9341fce0-5864-4b8a-a850-1ec0689482b3")
    val entity = RawPatient(id, List("123"), "John", "Doe", DateTime.parse("2000-01-01T00:00:00.000Z"))
    Post("/patients", entity) ~> route ~> check {
      status shouldBe Conflict
      patientRepo.findById(id).value shouldNot be(entity)
    }
  }

  it should "get a patient" in new TestFixture {
    val id = "ccbf8ccb-12b9-4f33-b5a2-cb2d38039d3e"
    Get(s"/patients/$id") ~> route ~> check {
      status shouldBe OK
      responseAs[RawPatient] shouldBe RawPatient(
        id = UUID.fromString("ccbf8ccb-12b9-4f33-b5a2-cb2d38039d3e"),
        identifiers = List("41915278"),
        firstName = "Patient",
        lastName = "Alpha",
        dateOfBirth = DateTime.parse("1980-01-10T00:00:00.000Z")
      )
    }
  }

  it should "return NotFound if trying to get a patient who does not exist" in new TestFixture {
    val id = UUID.randomUUID()
    Get(s"/patients/${id.toString}") ~> route ~> check {
      status shouldBe NotFound
    }
  }

  it should "update a patient" in new TestFixture {
    val id     = UUID.fromString("ccbf8ccb-12b9-4f33-b5a2-cb2d38039d3e")
    val entity = UpdatePatient(firstName = Some("John"))
    Put(s"/patients/${id.toString}", entity) ~> route ~> check {
      status shouldBe OK
      patientRepo.findById(id).value.firstName shouldBe "John"
    }
  }

  it should "return NotFound if trying to update a patient who does not exist" in new TestFixture {
    val id     = UUID.randomUUID()
    val entity = UpdatePatient(firstName = Some("John"))
    Put(s"/patients/${id.toString}", entity) ~> route ~> check {
      status shouldBe NotFound
    }
  }

  it should "delete a patient" in new TestFixture {
    val id = UUID.fromString("ccbf8ccb-12b9-4f33-b5a2-cb2d38039d3e")
    Delete(s"/patients/${id.toString}") ~> route ~> check {
      status shouldBe OK
      patientRepo.findById(id) shouldBe None
    }
  }

  it should "return NotFound if trying to delete a patient who does not exist" in new TestFixture {
    val id = UUID.randomUUID()
    Delete(s"/patients/${id.toString}") ~> route ~> check {
      status shouldBe NotFound
    }
  }

  it should "list all codes" in new TestFixture {
    Get("/codes") ~> route ~> check {
      status shouldBe OK
      responseAs[List[ResultCode]] shouldBe List(
        ResultCode("ALP", Some("6768-6"), Some("Alkaline phosphatase [Enzymatic activity/volume] in Serum or Plasma")),
        ResultCode("ALB", Some("1751-7"), Some("Albumin, Serum")),
        ResultCode("CA", Some("17861-6"), Some("Calcium, Serum")),
        ResultCode("xCCA", Some("18281-6"), Some("Corrected Calcium")),
        ResultCode("PHOS", Some("2777-1"), Some("Phosphorus (Inorganic), Serum"))
      )
    }
  }

  it should "create a new code" in new TestFixture {
    val entity = ResultCode("ABC", Some("123-4"), Some("Test Code"))
    Post("/codes", entity) ~> route ~> check {
      status shouldBe Created
      labResultCodeRepo.findByKey("ABC").value shouldBe entity
    }
  }

  it should "return Conflict if trying to create a code with an existing key" in new TestFixture {
    val entity = ResultCode("CA", Some("123-4"), Some("Test Code"))
    Post("/codes", entity) ~> route ~> check {
      status shouldBe Conflict
      labResultCodeRepo.findByKey("CA") shouldNot be(entity)
    }
  }

  it should "get a code" in new TestFixture {
    Get("/codes/CA") ~> route ~> check {
      status shouldBe OK
      responseAs[ResultCode] shouldBe ResultCode("CA", Some("17861-6"), Some("Calcium, Serum"))
    }
  }

  it should "return NotFound if trying to get a code which does not exist" in new TestFixture {
    Get("/codes/ABC") ~> route ~> check {
      status shouldBe NotFound
    }
  }

  it should "update a code" in new TestFixture {
    val entity = UpdateResultCode(Some("123-4"))
    Put("/codes/CA", entity) ~> route ~> check {
      status shouldBe OK
      labResultCodeRepo.findByKey("CA").value.code.value shouldBe "123-4"
    }
  }

  it should "return NotFound if trying to update a code which does not exist" in new TestFixture {
    val entity = UpdateResultCode(Some("123-4"))
    Put("/codes/ABC", entity) ~> route ~> check {
      status shouldBe NotFound
    }
  }

  it should "delete a code" in new TestFixture {
    Delete("/codes/CA") ~> route ~> check {
      status shouldBe OK
      labResultCodeRepo.findByKey("CA") shouldBe None
    }
  }

  it should "return NotFound if trying to delete a code which does not exist" in new TestFixture {
    Delete("/codes/ABC") ~> route ~> check {
      status shouldBe NotFound
    }
  }

  it should "list all results" in new TestFixture {
    Get("/results") ~> route ~> check {
      status shouldBe OK
      responseAs[List[RawLabResult]] shouldBe List(
        RawLabResult(
          id = "41915278-41860BONALP~55-BON-ALP-1407542400000",
          hospitalId = "41915278",
          sampleId = "41860BONALP~55",
          date = LocalDate.parse("2014-08-09"),
          profileName = "BONE PROFILE",
          profileCode = "BON",
          value = "55",
          testName = "ALP",
          unit = "IU/L",
          lower = Some(35.0),
          upper = Some(104.0)
        ),
        RawLabResult(
          id = "41915278-41860BONALP~55-BON-ALB-1407542400000",
          hospitalId = "41915278",
          sampleId = "41860BONALP~55",
          date = LocalDate.parse("2014-08-09"),
          profileName = "BONE PROFILE",
          profileCode = "BON",
          value = "37",
          testName = "ALB",
          unit = "g/L",
          lower = Some(34.0),
          upper = Some(50.0)
        ),
        RawLabResult(
          id = "41915278-41860BONALP~55-BON-CA-1407542400000",
          hospitalId = "41915278",
          sampleId = "41860BONALP~55",
          date = LocalDate.parse("2014-08-09"),
          profileName = "BONE PROFILE",
          profileCode = "BON",
          value = "2.18",
          testName = "CA",
          unit = "mmol/L",
          lower = Some(2.2),
          upper = Some(2.6)
        ),
        RawLabResult(
          id = "41915278-41860BONALP~55-BON-xCCA-1407542400000",
          hospitalId = "41915278",
          sampleId = "41860BONALP~55",
          date = LocalDate.parse("2014-08-09"),
          profileName = "BONE PROFILE",
          profileCode = "BON",
          value = "2.34",
          testName = "xCCA",
          unit = "mmol/L",
          lower = Some(2.2),
          upper = Some(2.6)
        ),
        RawLabResult(
          id = "41915278-41860BONALP~55-BON-PHOS-1407542400000",
          hospitalId = "41915278",
          sampleId = "41860BONALP~55",
          date = LocalDate.parse("2014-08-09"),
          profileName = "BONE PROFILE",
          profileCode = "BON",
          value = "1.29",
          testName = "PHOS",
          unit = "mmol/L",
          lower = Some(0.87),
          upper = Some(1.45)
        ),
        RawLabResult(
          id = "40681650-41860BONALP~55-BON-PHOS-1407542400000",
          hospitalId = "40681650",
          sampleId = "41860BONALP~55",
          date = LocalDate.parse("2014-08-09"),
          profileName = "BONE PROFILE",
          profileCode = "BON",
          value = "1.29",
          testName = "PHOS",
          unit = "mmol/L",
          lower = Some(0.87),
          upper = Some(1.45)
        )
      )
    }
  }

  it should "create a new result" in new TestFixture {
    val entity = RawLabResult(
      id = "123",
      hospitalId = "112233",
      sampleId = "SID55",
      date = LocalDate.parse("2017-01-01"),
      profileName = "Profile ABC",
      profileCode = "ABC",
      value = "40",
      testName = "Yolo",
      unit = "days",
      lower = Some(0.0),
      upper = Some(100.0)
    )
    Post("/results", entity) ~> route ~> check {
      status shouldBe Created
      labResultRepo.findById("123").value shouldBe entity
    }
  }

  it should "return Conflict if trying to create a result with an existing id" in new TestFixture {
    val id = "41915278-41860BONALP~55-BON-ALP-1407542400000"
    val entity = RawLabResult(
      id = id,
      hospitalId = "112233",
      sampleId = "SID55",
      date = LocalDate.parse("2017-01-01"),
      profileName = "Profile ABC",
      profileCode = "ABC",
      value = "40",
      testName = "Yolo",
      unit = "days",
      lower = Some(0.0),
      upper = Some(100.0)
    )
    Post("/results", entity) ~> route ~> check {
      status shouldBe Conflict
      labResultRepo.findById(id).value shouldNot be(entity)
    }
  }

  it should "get a result" in new TestFixture {
    val id = "41915278-41860BONALP~55-BON-ALP-1407542400000"
    Get(s"/results/$id") ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "return NotFound if trying to get a result which does not exist" in new TestFixture {
    val id = "XYZ"
    Get(s"/results/$id") ~> route ~> check {
      status shouldBe NotFound
    }
  }

  it should "update a result" in new TestFixture {
    val id     = "41915278-41860BONALP~55-BON-ALP-1407542400000"
    val entity = UpdateLabResult(value = Some("999"))
    Put(s"/results/$id", entity) ~> route ~> check {
      status shouldBe OK
      labResultRepo.findById(id).value.value shouldBe "999"
    }
  }

  it should "return NotFound if trying to update a result which does not exist" in new TestFixture {
    val id     = "ABC"
    val entity = UpdateLabResult(value = Some("999"))
    Put(s"/results/$id", entity) ~> route ~> check {
      status shouldBe NotFound
    }
  }

  it should "delete a result" in new TestFixture {
    val id = "41915278-41860BONALP~55-BON-ALP-1407542400000"
    Delete(s"/results/$id") ~> route ~> check {
      status shouldBe OK
      labResultRepo.findById(id) shouldBe empty
    }
  }

  it should "return NotFound if trying to delete a result which does not exist" in new TestFixture {
    val id = "ABC"
    Delete(s"/results/$id") ~> route ~> check {
      status shouldBe NotFound
    }
  }
}
