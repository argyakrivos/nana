package me.akrivos.nana
package repository

import me.akrivos.nana.model.ResultCode
import org.scalatest.{FlatSpecLike, Matchers, OptionValues}

class LabResultCodeRepositorySpec extends FlatSpecLike with Matchers with OptionValues {

  class TestFixture(path: String = "/labresults-codes.csv") {
    val repo = new LabResultCodeRepository(getClass.getResource(path))
  }

  behavior of "LabResultCodeRepository"

  it should "have 133 entries" in new TestFixture {
    repo.toIterable should have size 133
  }

  it should "find code by key" in new TestFixture {
    repo.findByKey("WCC").value shouldBe ResultCode("WCC", Some("6690-2"), Some("White Cell Count"))
  }

  it should "not find an unknown key" in new TestFixture {
    repo.findByKey("ABC") shouldBe empty
  }

  it should "have no entries if there is an error during parsing" in new TestFixture("/idonotexist.csv") {
    repo.toIterable shouldBe empty
  }

  it should "not contain duplicates" in new TestFixture("/test-codes.csv") {
    repo.toIterable.toList should contain theSameElementsAs List(
      ResultCode("ALP", Some("6768-6"), Some("Alkaline phosphatase [Enzymatic activity/volume] in Serum or Plasma")),
      ResultCode("ALB", Some("1751-7"), Some("Albumin, Serum")),
      ResultCode("CA", Some("17861-6"), Some("Calcium, Serum")),
      ResultCode("xCCA", Some("18281-6"), Some("Corrected Calcium")),
      ResultCode("PHOS", Some("2777-1"), Some("Phosphorus (Inorganic), Serum"))
    )
  }
}
