package org.downtowndailybread.bethsaida.json

import java.time.LocalDate

import org.downtowndailybread.bethsaida.model._
import spray.json._
import DefaultJsonProtocol._
import org.downtowndailybread.bethsaida.providers.{DatabaseConnectionProvider, SettingsProvider}


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
            o.get("intakeDate").map(_.convertTo[LocalDate])
          )
      }
    }
  }


  implicit val clientFormat = jsonFormat12(Client)

  implicit val seqClientFormat = seqFormat[Client]
}

