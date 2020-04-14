package org.downtowndailybread.bethsaida.json

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}

import org.downtowndailybread.bethsaida.model._
import org.downtowndailybread.bethsaida.providers.{DatabaseConnectionProvider, SettingsProvider}
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.util.Try


trait ClientJson extends BaseSupport {
  this: DatabaseConnectionProvider with SettingsProvider =>

  implicit val newClientFormat = new RootJsonReader[UpsertClient] {
    override def read(json: JsValue): UpsertClient = {
      json match {
        case JsObject(o) =>
          UpsertClient(
            o.get("firstName").map(_.convertTo[String]),
            o.get("middleName").map(_.convertTo[String]),
            o.get("lastName").map(_.convertTo[String]),
            o.get("dateOfBirth").map(_.convertTo[LocalDate]),
            o.get("gender").map(_.convertTo[Gender]),
            o.get("race").map(_.convertTo[Race]),
            o.get("phone").map(_.convertTo[String]),
            o.get("clientPhoto").map(_.convertTo[String]),
            o.get("photoId").map(_.convertTo[String]),
            o.get("intakeDate").map(_.convertTo[LocalDate]),
            o.get("raceSecondary").map(_.convertTo[Race]),
            o.get("hispanic").map(_.convertTo[Boolean])
          )
      }
    }
  }


  implicit val clientFormat = jsonFormat16(Client)

  implicit val seqClientFormat = seqFormat[Client]


  implicit val banAttribFormat = new RootJsonFormat[BanAttribute] {
    override def write(obj: BanAttribute): JsValue = {
      val stop = obj.stopTime match {
        case Some(date) => Map("stop" -> zonedDateTimeFormat.write(date))
        case _ => Map[String, JsValue]()
      }

      val main = Map[String, JsValue](
        "banType" -> obj.banType.str,
        "notes" -> obj.notes,
        "start" -> zonedDateTimeFormat.write(obj.startTime)
      )

      JsObject(main ++ stop)
    }


    override def read(json: JsValue): BanAttribute = {
      val obj = json.asJsObject.fields
      BanAttribute(
        zonedDateTimeFormat.read(obj("start")).withZoneSameInstant(zone).toLocalDateTime,
        Try(obj.get("stop").map(_.convertTo[ZonedDateTime]).map(_.withZoneSameInstant(zone).toLocalDateTime)).toOption.flatten,
        BanType(obj("banType").convertTo[String]),
        obj.get("notes").map(_.convertTo[String])
      )
    }
  }

  implicit val banAttribSeqFormat = seqFormat[BanAttribute]

  implicit val banFormat = new RootJsonWriter[Ban] {
    override def write(obj: Ban): JsValue = {
      val map = banAttribFormat.write(obj.attributes).asJsObject.fields

      val others = Map[String, JsValue](
        "id" -> uuidFormat.write(obj.id),
        "client_id" -> uuidFormat.write(obj.clientId),
        "user_id" -> uuidFormat.write(obj.user.id),
        "active" -> JsBoolean(obj.attributes.isActive(LocalDateTime.now()))
      )

      JsObject(map ++ others)
    }
  }

  implicit val banSeqFormat = new RootJsonWriter[Seq[Ban]] {
    override def write(obj: Seq[Ban]): JsValue = JsArray(
      obj.map(banFormat.write).toVector
    )
  }
}

