package me.akrivos.nana
package repository

import java.net.URL

import com.typesafe.scalalogging.StrictLogging
import kantan.codecs.Result
import kantan.csv._
import kantan.csv.ops._
import me.akrivos.nana.model.RawLabResult

import scala.util.Try

class LabResultRepository(csvFile: URL) extends StrictLogging {

  private val repo: List[RawLabResult] =
    Result.sequence {
      csvFile
        .asCsvReader[Vector[String]](rfc.withHeader)
        .map { res =>
          res.map { row =>
            RawLabResult(
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

  def toIterable: Iterable[RawLabResult] = repo

  def findByHospitalId(hospitalId: String): List[RawLabResult] = {
    repo.filter(_.hospitalId.equalsIgnoreCase(hospitalId))
  }
}
