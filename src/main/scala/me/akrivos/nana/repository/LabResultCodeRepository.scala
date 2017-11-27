package me.akrivos.nana
package repository

import java.net.URL

import com.typesafe.scalalogging.StrictLogging
import kantan.codecs.Result
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._
import me.akrivos.nana.model.ResultCode

class LabResultCodeRepository(csvFile: URL) extends StrictLogging {

  private val repo: List[ResultCode] =
    Result.sequence(csvFile.readCsv[List, ResultCode](rfc.withHeader)) match {
      case Success(list) => list.distinct
      case Failure(e) =>
        logger.error(s"Could not parse data from $csvFile", e)
        List.empty[ResultCode]
    }

  def list: Iterable[ResultCode] = repo

  def findByKey(key: String): Option[ResultCode] = {
    repo.find(_.key.equalsIgnoreCase(key))
  }
}
