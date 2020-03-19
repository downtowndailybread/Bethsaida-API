package org.downtowndailybread.bethsaida.json

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, LocalTime, OffsetDateTime, ZoneId, ZoneOffset, ZonedDateTime}
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

  implicit def mapStringConverter(map: Map[String, String]): Map[String, JsValue] = {
    map.map {
      case (k, v) => (k -> JsString(v))
    }
  }

  implicit val uuidFormat = new RootJsonFormat[UUID] {
    override def read(json: JsValue): UUID = json match {
      case JsString(s) => parseUUID(s)
      case s => throw new MalformedJsonErrorException(s"could not parse UUID: $s")
    }

    override def write(obj: UUID): JsValue = JsString(obj)
  }

  implicit def seqFormat[T: JsonFormat](implicit format: JsonFormat[T]): RootJsonFormat[Seq[T]] =
    new RootJsonFormat[Seq[T]] {
      override def read(json: JsValue): Seq[T] = json match {
        case JsArray(arr) => arr.map(arrVal => format.read(arrVal)).toList
        case s => throw new MalformedJsonErrorException(s"could not seq: $s")
      }

      override def write(obj: Seq[T]): JsValue = JsArray(obj.map(format.write).toVector)
    }

  implicit def seqWriter[T: JsonWriter](implicit format: RootJsonWriter[T]): RootJsonWriter[Seq[T]] =
    new RootJsonWriter[Seq[T]] {
      override def write(obj: Seq[T]): JsValue = {
        JsArray(obj.map(format.write).toVector)
      }
    }

  implicit def localDateTimeToZone(d: LocalDateTime): ZonedDateTime = {
    ZonedDateTime.of(
      d,
      ZoneId.of("America/New_York")
    )
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
        case JsString(s) => LocalDate.parse(s)
      }
    }
  }

  val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSVV")
  val zone = ZoneId.of("America/New_York")

  implicit val localDateTimeFormat = new RootJsonFormat[LocalDateTime] {
    override def write(obj: LocalDateTime): JsValue = obj.format(formatter)

    override def read(json: JsValue): LocalDateTime = {
      (json: @unchecked) match {
        case JsString(o) => LocalDateTime.parse(o, formatter)
      }
    }
  }

  implicit val zonedDateTimeFormat = new RootJsonFormat[ZonedDateTime] {
    override def write(obj: ZonedDateTime): JsValue = {
      JsString(obj.withZoneSameInstant(zone).withZoneSameInstant(ZoneId.of("Z")).format(formatter))
    }

    override def read(json: JsValue): ZonedDateTime = {
//      val localDate = LocalDateTime.parse(json.convertTo[String], DateTimeFormatter.ofPattern("M/d/u, h:m:s a"))
//      val resp = ZonedDateTime.of(localDate, zone)
//      resp
      ZonedDateTime.parse(json.convertTo[String], formatter)
    }
  }
}
