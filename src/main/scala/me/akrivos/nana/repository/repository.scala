package me.akrivos.nana

import cats.syntax.either._
import com.github.nscala_time.time.Imports._
import io.circe._
import org.joda.time.format.ISODateTimeFormat

package object repository {

  val DefaultDateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC()
  val DefaultDateFormatter     = DateTimeFormat.forPattern("dd/MM/yyyy")

  implicit val encodeDateTime: Encoder[DateTime] =
    Encoder.encodeString.contramap[DateTime](DefaultDateTimeFormatter.print)

  implicit val decodeDateTime: Decoder[DateTime] =
    Decoder.decodeString.emap { str =>
      Either.catchNonFatal(DefaultDateTimeFormatter.parseDateTime(str)).leftMap(_ => "DateTime")
    }
}
