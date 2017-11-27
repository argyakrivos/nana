package me.akrivos.nana
package repository

import com.github.nscala_time.time.Imports._
import me.akrivos.nana.model.RawLabResult
import org.scalatest.{FlatSpecLike, Matchers}

class LabResultRepositorySpec extends FlatSpecLike with Matchers {

  class TestFixture(path: String = "/test-results.csv") {
    val repo = new LabResultRepository(getClass.getResource(path))
  }

  behavior of "LabResultRepository"

  it should "have 6 entries" in new TestFixture {
    repo.list should have size 6
  }

  it should "find test results by hospitalId" in new TestFixture {
    repo.findByHospitalId("41915278") shouldBe List(
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
      )
    )
  }

  it should "not find an unknown hospitalId" in new TestFixture {
    repo.findByHospitalId("ABC") shouldBe empty
  }

  it should "have no entries if there is an error during parsing" in new TestFixture("/idonotexist.csv") {
    repo.list shouldBe empty
  }

  it should "not contain duplicates" in new TestFixture {
    repo.findByHospitalId("40681650") shouldBe List(
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
