package me.akrivos.nana
package api

import java.util.UUID

import com.twitter.finagle.http.Response
import io.circe.generic.auto._
import io.finch.Endpoint._
import io.finch._
import io.finch.circe.jacksonSerializer._
import me.akrivos.nana.model._
import me.akrivos.nana.repository._
import me.akrivos.nana.service.LabResultService

class FinApi(
  labResultCodeRepo: LabResultCodeRepository,
  labResultRepo: LabResultRepository,
  patientRepo: PatientRepository,
  labResultService: LabResultService
) {

  lazy val routes = {
    getPatients :+: newPatient :+: getPatient :+: updatePatient :+: deletePatient :+:
    getCodes    :+: newCode    :+: getCode    :+: updateCode    :+: deleteCode    :+:
    getResults  :+: newResult  :+: getResult  :+: updateResult  :+: deleteResult
  }

  private val getPatients: Endpoint[Response] =
    get("patients" :: paramOption("withResults").as[Boolean].withDefault(false)) { withResults: Boolean =>
      if (withResults) {
        Ok {
          PatientsResults(
            patientRepo.list.flatMap { patient =>
              labResultService.getResultsForPatientId(patient.id)
            }
          )
        }.toResponse
      } else {
        Ok(patientRepo.list).toResponse
      }
    }

  private val newPatient: Endpoint[Unit] =
    post("patients" :: jsonBody[RawPatient]) { entity: RawPatient =>
      if (patientRepo.insert(entity))
        Created(())
      else
        Conflict(new IllegalArgumentException(s"${entity.id} already exists"))
    }

  private val getPatient: Endpoint[RawPatient] =
    get("patients" :: path[UUID]) { id: UUID =>
      patientRepo.findById(id) match {
        case Some(patient) => Ok(patient)
        case None          => NotFound(new IllegalArgumentException(s"$id does not exist"))
      }
    }

  private val updatePatient: Endpoint[Unit] =
    put("patients" :: path[UUID] :: jsonBody[UpdatePatient]) { (id: UUID, entity: UpdatePatient) =>
      if (patientRepo.update(id, entity))
        Ok(())
      else
        NotFound(new IllegalArgumentException(s"$id does not exist"))
    }

  private val deletePatient: Endpoint[Unit] =
    delete("patients" :: path[UUID]) { id: UUID =>
      if (patientRepo.delete(id))
        Ok(())
      else
        NotFound(new IllegalArgumentException(s"$id does not exist"))
    }

  private val getCodes: Endpoint[Iterable[ResultCode]] =
    get("codes") {
      Ok(labResultCodeRepo.list)
    }

  private val newCode: Endpoint[Unit] =
    post("codes" :: jsonBody[ResultCode]) { entity: ResultCode =>
      if (labResultCodeRepo.insert(entity))
        Created(())
      else
        Conflict(new IllegalArgumentException(s"${entity.code} already exists"))
    }

  private val getCode: Endpoint[ResultCode] =
    get("codes" :: path[String]) { key: String =>
      labResultCodeRepo.findByKey(key) match {
        case Some(code) => Ok(code)
        case None       => NotFound(new IllegalArgumentException(s"$key does not exist"))
      }
    }

  private val updateCode: Endpoint[Unit] =
    put("codes" :: path[String] :: jsonBody[UpdateResultCode]) { (key: String, entity: UpdateResultCode) =>
      if (labResultCodeRepo.update(key, entity))
        Ok(())
      else
        NotFound(new IllegalArgumentException(s"$key does not exist"))
    }

  private val deleteCode: Endpoint[Unit] =
    delete("codes" :: path[String]) { key: String =>
      if (labResultCodeRepo.delete(key))
        Ok(())
      else
        NotFound(new IllegalArgumentException(s"$key does not exist"))
    }

  private val getResults: Endpoint[Iterable[RawLabResult]] =
    get("results") {
      Ok(labResultRepo.list)
    }

  private val newResult: Endpoint[Unit] =
    post("results" :: jsonBody[RawLabResult]) { entity: RawLabResult =>
      if (labResultRepo.insert(entity))
        Created(())
      else
        Conflict(new IllegalArgumentException(s"${entity.id} already exists"))
    }

  private val getResult: Endpoint[RawLabResult] =
    get("results" :: path[String]) { id: String =>
      labResultRepo.findById(id) match {
        case Some(result) => Ok(result)
        case None         => NotFound(new IllegalArgumentException(s"$id does not exist"))
      }
    }

  private val updateResult: Endpoint[Unit] =
    put("results" :: path[String] :: jsonBody[UpdateLabResult]) { (id: String, entity: UpdateLabResult) =>
      if (labResultRepo.update(id, entity))
        Ok(())
      else
        NotFound(new IllegalArgumentException(s"$id does not exist"))
    }

  private val deleteResult: Endpoint[Unit] =
    delete("results" :: path[String]) { id: String =>
      if (labResultRepo.delete(id))
        Ok(())
      else
        NotFound(new IllegalArgumentException(s"$id does not exist"))
    }
}
