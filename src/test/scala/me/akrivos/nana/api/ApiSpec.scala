package me.akrivos.nana
package api

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.nscala_time.time.Imports._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import me.akrivos.nana.model._
import org.scalatest.{FlatSpecLike, Matchers}

class ApiSpec extends FlatSpecLike with Matchers with ScalatestRouteTest with FailFastCirceSupport {

  trait TestFixture {
    val route: Route = new Api().route
  }

  behavior of "API"

  it should "list all patients without results" in new TestFixture {
    Get("/patients?withResults=false") ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "list all patients with results" in new TestFixture {
    Get("/patients?withResults=true") ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "create a new patient" in new TestFixture {
    val id     = UUID.randomUUID()
    val entity = RawPatient(id, List("123"), "John", "Doe", DateTime.parse("2000-01-01T00:00:00.000Z"))
    Post("/patients", entity) ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "get a patient" in new TestFixture {
    val id = "ccbf8ccb-12b9-4f33-b5a2-cb2d38039d3e"
    Get(s"/patients/$id") ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "update a patient" in new TestFixture {
    val id     = "ccbf8ccb-12b9-4f33-b5a2-cb2d38039d3e"
    val entity = UpdatePatient(firstName = Some("John"))
    Put(s"/patients/$id", entity) ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "delete a patient" in new TestFixture {
    val id = "ccbf8ccb-12b9-4f33-b5a2-cb2d38039d3e"
    Delete(s"/patients/$id") ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "list all codes" in new TestFixture {
    Get("/codes") ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "create a new code" in new TestFixture {
    val entity = ResultCode("ABC", Some("123-4"), Some("Test Code"))
    Post("/codes", entity) ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "get a code" in new TestFixture {
    Get("/codes/CA") ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "update a code" in new TestFixture {
    val entity = UpdateResultCode(Some("123-4"), Some("Test Code"))
    Put("/codes/CA", entity) ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "delete a code" in new TestFixture {
    Delete("/codes/CA") ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "list all results" in new TestFixture {
    Get("/results") ~> route ~> check {
      status shouldBe OK
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
      status shouldBe OK
    }
  }

  it should "get a result" in new TestFixture {
    val id = "41915278-41860BONALP~55-BON-ALP-1407542400000"
    Get(s"/results/$id") ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "update a result" in new TestFixture {
    val id     = "41915278-41860BONALP~55-BON-ALP-1407542400000"
    val entity = UpdateLabResult(value = Some("999"))
    Put(s"/results/$id", entity) ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "delete a result" in new TestFixture {
    val id = "41915278-41860BONALP~55-BON-ALP-1407542400000"
    Delete(s"/results/$id") ~> route ~> check {
      status shouldBe OK
    }
  }
}
