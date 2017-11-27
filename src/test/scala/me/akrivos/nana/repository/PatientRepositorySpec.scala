package me.akrivos.nana
package repository

import java.util.UUID

import com.github.nscala_time.time.Imports._
import me.akrivos.nana.model.RawPatient
import org.scalatest.{FlatSpecLike, Matchers, OptionValues}

class PatientRepositorySpec extends FlatSpecLike with Matchers with OptionValues {

  class TestFixture(path: String = "/patients.json") {
    val repo = new PatientRepository(getClass.getResource(path))
  }

  behavior of "PatientRepository"

  it should "have 4 entries" in new TestFixture {
    repo.list should have size 4
  }

  it should "find patient by id" in new TestFixture {
    val id = UUID.fromString("22eef0c0-0c53-4710-bbb3-6bde0a5b5396")
    repo.findById(id).value shouldBe RawPatient(
      id = id,
      identifiers = List("41601442"),
      firstName = "Patient",
      lastName = "Gamma",
      dateOfBirth = DateTime.parse("1942-12-24T23:00:00.000Z")
    )
  }

  it should "not find an unknown key" in new TestFixture {
    repo.findById(UUID.randomUUID()) shouldBe empty
  }

  it should "have no entries if there is an error during parsing" in new TestFixture("/idonotexist.json") {
    repo.list shouldBe empty
  }

  it should "not contain duplicates" in new TestFixture("/test-patients.json") {
    repo.list.toList should contain theSameElementsAs List(
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
