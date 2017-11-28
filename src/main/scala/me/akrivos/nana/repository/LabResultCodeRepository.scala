package me.akrivos.nana
package repository

import java.net.URL

import com.typesafe.scalalogging.StrictLogging
import kantan.codecs.Result
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._
import me.akrivos.nana.model.{ResultCode, UpdateResultCode}

class LabResultCodeRepository(csvFile: URL) extends StrictLogging {

  private var repo: List[ResultCode] =
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

  def insert(code: ResultCode): Boolean = {
    if (!repo.exists(_.key == code.key)) {
      repo = code :: repo
      true
    } else {
      false
    }
  }

  def update(key: String, code: UpdateResultCode): Boolean = {
    findByKey(key) match {
      case Some(oldCode) =>
        val updatedCode = ResultCode(
          key = key,
          code = code.code.orElse(oldCode.code),
          description = code.description.orElse(oldCode.description)
        )
        repo = updatedCode :: repo.filterNot(_ == updatedCode)
        true
      case None =>
        false
    }
  }

  def delete(key: String): Boolean = {
    val oldSize = repo.size
    repo = repo.filterNot(_.key == key)
    repo.size < oldSize
  }
}
