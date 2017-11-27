package me.akrivos.nana
package repository

import java.net.URL
import java.util.UUID

import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._
import io.circe.parser._
import me.akrivos.nana.model.RawPatient

import scala.io.Source
import scala.util.Try

class PatientRepository(jsonFile: URL) extends StrictLogging {

  private val repo: List[RawPatient] =
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
}
