package me.akrivos.nana
package service

import java.util.UUID

import com.github.nscala_time.time.Imports._
import me.akrivos.nana.model._
import me.akrivos.nana.repository._

class LabResultService(
  labResultCodeRepo: LabResultCodeRepository,
  labResultRepo: LabResultRepository,
  patientRepo: PatientRepository,
) {

  def getResultsForPatientId(patientId: UUID): Option[PatientResults] = {
    patientRepo.findById(patientId).map { patient =>
      PatientResults(
        id = patient.id,
        firstName = patient.firstName,
        lastName = patient.lastName,
        dob = patient.dateOfBirth,
        lab_results = patient.identifiers
          .flatMap(identifier => labResultRepo.findByHospitalId(identifier))
          .groupBy(lr => (lr.date, lr.profileName, lr.profileCode))
          .map {
            case ((date, profileName, profileCode), results) =>
              LabResult(
                timestamp = date.toDateTime(LocalTime.Midnight, DateTimeZone.UTC),
                profile = Profile(
                  name = profileName,
                  code = profileCode
                ),
                panel = results.map { rr =>
                  val resultCode = labResultCodeRepo.findByKey(rr.testName)
                  PanelResult(
                    code = resultCode.flatMap(_.code).getOrElse(""),
                    label = resultCode.flatMap(_.description).getOrElse(""),
                    value = rr.value,
                    unit = rr.unit,
                    lower = rr.lower,
                    upper = rr.upper
                  )
                }
              )
          }
          .toList
      )
    }
  }
}
