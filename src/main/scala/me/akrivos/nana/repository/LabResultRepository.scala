package me.akrivos.nana
package repository

import java.net.URL

import com.typesafe.scalalogging.StrictLogging
import kantan.codecs.Result
import kantan.csv._
import kantan.csv.ops._
import me.akrivos.nana.model.{RawLabResult, UpdateLabResult}

import scala.util.Try

class LabResultRepository(csvFile: URL) extends StrictLogging {

  private var repo: List[RawLabResult] =
    Result.sequence {
      csvFile
        .asCsvReader[Vector[String]](rfc.withHeader)
        .map { res =>
          res.map { row =>
            RawLabResult(
              id = generateId(row),
              hospitalId = row(0),
              sampleId = row(1),
              date = DefaultDateFormatter.parseLocalDate(row(2)),
              profileName = row(3),
              profileCode = row(4),
              value = row
                .slice(5, 30)
                .find(r => r.nonEmpty && r.startsWith(row(30)))
                .flatMap(_.split("~").lastOption)
                .getOrElse(""),
              testName = row(30),
              unit = row(31),
              lower = row.lift(32).flatMap(s => Try(s.toDouble).toOption),
              upper = row.lift(33).flatMap(s => Try(s.toDouble).toOption)
            )
          }
        }
        .toList
        .distinct
    } match {
      case Success(list) => list
      case Failure(e) =>
        logger.error(s"Could not parse data from $csvFile", e)
        List.empty[RawLabResult]
    }

  private def generateId(row: Vector[String]): String = {
    val hospitalId  = row(0)
    val sampleId    = row(1)
    val profileCode = row(4)
    val testName    = row(30)
    val date        = DefaultDateFormatter.parseDateTime(row(2)).getMillis
    s"$hospitalId-$sampleId-$profileCode-$testName-$date"
  }

  def list: Iterable[RawLabResult] = repo

  def findById(id: String): Option[RawLabResult] = {
    repo.find(_.id == id)
  }

  def findByHospitalId(hospitalId: String): List[RawLabResult] = {
    repo.filter(_.hospitalId.equalsIgnoreCase(hospitalId))
  }

  def insert(result: RawLabResult): Boolean = {
    if (!repo.exists(_.id == result.id)) {
      repo = result :: repo
      true
    } else {
      false
    }
  }

  def update(id: String, result: UpdateLabResult): Boolean = {
    findById(id) match {
      case Some(oldResult) =>
        val updatedResult = RawLabResult(
          id = id,
          hospitalId = result.hospitalId.getOrElse(oldResult.hospitalId),
          sampleId = result.sampleId.getOrElse(oldResult.sampleId),
          date = result.date.getOrElse(oldResult.date),
          profileName = result.profileName.getOrElse(oldResult.profileName),
          profileCode = result.profileCode.getOrElse(oldResult.profileCode),
          value = result.value.getOrElse(oldResult.value),
          testName = result.testName.getOrElse(oldResult.testName),
          unit = result.unit.getOrElse(oldResult.unit),
          lower = result.lower.orElse(oldResult.lower),
          upper = result.upper.orElse(oldResult.upper)
        )
        repo = updatedResult :: repo.filterNot(_ == updatedResult)
        true
      case None =>
        false
    }
  }

  def delete(id: String): Boolean = {
    val oldSize = repo.size
    repo = repo.filterNot(_.id == id)
    repo.size < oldSize
  }
}
