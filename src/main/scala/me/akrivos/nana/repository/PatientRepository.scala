package me.akrivos.nana
package repository

import java.net.URL
import java.util.UUID

import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._
import io.circe.parser._
import me.akrivos.nana.model.{RawPatient, UpdatePatient}

import scala.io.Source
import scala.util.Try

class PatientRepository(jsonFile: URL) extends StrictLogging {

  private var repo: List[RawPatient] =
    Try(Source.fromURL(jsonFile).mkString).toEither.flatMap { json =>
      decode[List[RawPatient]](json)
    } match {
      case Right(list) => list.distinct
      case Left(e) =>
        logger.error(s"Could not parse data from $jsonFile", e)
        List.empty[RawPatient]
    }

  def list: Iterable[RawPatient] = repo

  def findById(id: UUID): Option[RawPatient] = {
    repo.find(_.id == id)
  }

  def insert(patient: RawPatient): Boolean = {
    if (!repo.exists(_.id == patient.id)) {
      repo = patient :: repo
      true
    } else {
      false
    }
  }

  def update(id: UUID, patient: UpdatePatient): Boolean = {
    findById(id) match {
      case Some(oldPatient) =>
        val updatedPatient = RawPatient(
          id = id,
          identifiers = patient.identifiers.getOrElse(oldPatient.identifiers),
          firstName = patient.firstName.getOrElse(oldPatient.firstName),
          lastName = patient.lastName.getOrElse(oldPatient.lastName),
          dateOfBirth = patient.dateOfBirth.getOrElse(oldPatient.dateOfBirth)
        )
        repo = updatedPatient :: repo.filterNot(_ == oldPatient)
        true
      case None =>
        false
    }
  }

  def delete(id: UUID): Boolean = {
    val oldSize = repo.size
    repo = repo.filterNot(_.id == id)
    repo.size < oldSize
  }
}
