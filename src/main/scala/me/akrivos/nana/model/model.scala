package me.akrivos.nana

import java.util.UUID

import com.github.nscala_time.time.Imports._

package model {

  case class UserInfo(
    name: String,
    username: String,
    givenName: String,
    familyName: String,
    email: String
  )

  case class ResultCode(
    key: String,
    code: Option[String],
    description: Option[String]
  )

  case class UpdateResultCode(
    code: Option[String] = None,
    description: Option[String] = None
  )

  case class RawPatient(
    id: UUID,
    identifiers: List[String],
    firstName: String,
    lastName: String,
    dateOfBirth: DateTime
  )

  case class UpdatePatient(
    identifiers: Option[List[String]] = None,
    firstName: Option[String] = None,
    lastName: Option[String] = None,
    dateOfBirth: Option[DateTime] = None
  )

  case class RawLabResult(
    id: String,
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

  case class UpdateLabResult(
    hospitalId: Option[String] = None,
    sampleId: Option[String] = None,
    date: Option[LocalDate] = None,
    profileName: Option[String] = None,
    profileCode: Option[String] = None,
    value: Option[String] = None,
    testName: Option[String] = None,
    unit: Option[String] = None,
    lower: Option[Double] = None,
    upper: Option[Double] = None
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
