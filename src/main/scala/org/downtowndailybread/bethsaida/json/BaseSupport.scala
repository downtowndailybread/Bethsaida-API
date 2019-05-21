package org.downtowndailybread.bethsaida.json

import java.time.{LocalDate, LocalDateTime, LocalTime, OffsetDateTime, ZoneOffset, ZonedDateTime}
import java.util.UUID

import org.downtowndailybread.bethsaida.exception.MalformedJsonErrorException
import spray.json._
import DefaultJsonProtocol._
import org.downtowndailybread.bethsaida.providers.UUIDProvider

trait BaseSupport extends UUIDProvider {

  implicit def intConverter(i: Int): JsValue = JsNumber(i)
  implicit def stringConverter(s: String): JsValue = JsString(s)
  implicit def stringOptionConverter(s: Option[String]): JsValue = s match {
    case Some(s) => JsString(s)
    case None => JsNull
  }
  implicit def boolConverter(b: Boolean): JsValue = JsBoolean(b)

  implicit val uuidFormat = new RootJsonFormat[UUID] {
    override def read(json: JsValue): UUID = json match {
      case JsString(s) => parseUUID(s)
      case s => throw new MalformedJsonErrorException(s"could not parse UUID: $s")
    }

    override def write(obj: UUID): JsValue = JsString(obj)
  }

  implicit def seqFormat[T : JsonFormat](implicit format: JsonFormat[T]): RootJsonFormat[Seq[T]] =
    new RootJsonFormat[Seq[T]] {
    override def read(json: JsValue): Seq[T] = json match {
      case JsArray(arr) => arr.map(arrVal => format.read(arrVal)).toList
      case s => throw new MalformedJsonErrorException(s"could not seq: $s")
    }

    override def write(obj: Seq[T]): JsValue = JsArray(obj.map(format.write).toVector)
  }

  implicit def seqWriter[T: JsonWriter](implicit format: JsonWriter[T]): JsonWriter[Seq[T]] = new JsonWriter[Seq[T]] {
    override def write(obj: Seq[T]): JsValue = {
      JsArray(obj.map(format.write).toVector)
    }
  }


  implicit val localTimeFormat = new RootJsonFormat[LocalTime] {
    override def write(obj: LocalTime): JsValue = {
      JsObject(
        ("hour", obj.getHour),
        ("minute", obj.getMinute),
        ("second", obj.getSecond)
      )
    }

    override def read(json: JsValue): LocalTime = {
      (json: @unchecked) match {
        case JsObject(o) => LocalTime.of(
          o("hour").convertTo[Int],
          o("minute").convertTo[Int],
          o("second").convertTo[Int]
        )
      }
    }
  }

  implicit val localDateFormat = new RootJsonFormat[LocalDate] {
    override def write(obj: LocalDate): JsValue = JsObject(
      ("year", obj.getYear),
      ("month", obj.getMonthValue),
      ("day", obj.getDayOfMonth)
    )
    override def read(json: JsValue): LocalDate = {
      (json: @unchecked) match {
        case JsObject(o) => LocalDate.of(
          o("year").convertTo[Int],
          o("month").convertTo[Int],
          o("day").convertTo[Int]
        )
      }
    }
  }

  implicit val localDateTimeFormat = new RootJsonFormat[LocalDateTime] {
    override def write(obj: LocalDateTime): JsValue = JsObject(
      ("date", localDateFormat.write(obj.toLocalDate)),
      ("time", localTimeFormat.write(obj.toLocalTime))
    )

    override def read(json: JsValue): LocalDateTime = {
      (json: @unchecked) match {
        case JsObject(o) => LocalDateTime.of(
          localDateFormat.read(o("date")),
          localTimeFormat.read(o("time"))
        )
      }
    }
  }

  implicit val offsetDateTimeFormat = new RootJsonFormat[OffsetDateTime] {
    override def write(obj: OffsetDateTime): JsValue = JsObject(
      ("datetime", localDateTimeFormat.write(obj.toLocalDateTime)),
      ("offset", obj.getOffset.getId)
    )

    override def read(json: JsValue): OffsetDateTime = {
      (json: @unchecked) match {
        case JsObject(o) => OffsetDateTime.of(
          localDateTimeFormat.read(o("datetime")),
          ZoneOffset.of(o("offset").convertTo[String])
        )
      }
    }
  }

  implicit val zonedDateTime = new RootJsonFormat[ZonedDateTime] {
    override def write(obj: ZonedDateTime): JsValue = {
      offsetDateTimeFormat.write(obj.toOffsetDateTime)
    }

    override def read(json: JsValue): ZonedDateTime = {
      offsetDateTimeFormat.read(json).toZonedDateTime
    }
  }
}
