package me.akrivos.nana

import java.util.UUID

import com.github.nscala_time.time.Imports._

package model {

  case class ResultCode(
    key: String,
    code: Option[String],
    description: Option[String]
  )

  case class RawPatient(
    id: UUID,
    identifiers: List[String],
    firstName: String,
    lastName: String,
    dateOfBirth: DateTime
  )

  case class RawLabResult(
    hospitalId: String,
    sampleId: String,
    date: LocalDate,
    profileName: String,
    profileCode: String,
    value: String,
    testName: String,
    unit: String,
    lower: Option[Double],
    upper: Option[Double]
  )

  case class Profile(name: String, code: String)

  case class PanelResult(
    code: String,
    label: String,
    value: String,
    unit: String,
    lower: Option[Double],
    upper: Option[Double]
  )

  case class LabResult(
    timestamp: DateTime,
    profile: Profile,
    panel: List[PanelResult]
  )

  case class PatientResults(
    id: UUID,
    firstName: String,
    lastName: String,
    dob: DateTime,
    lab_results: List[LabResult]
  )

  case class PatientsResults(patients: Iterable[PatientResults])

}
