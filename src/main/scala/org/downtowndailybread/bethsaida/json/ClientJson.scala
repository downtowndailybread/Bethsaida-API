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
            o.get("nicknames").map(_.convertTo[Seq[String]]),
            o.get("dateOfBirth").map(_.convertTo[LocalDate]),
            o.get("photoIdTag").map(_.convertTo[String])
          )
      }
    }
  }


  implicit val clientFormat = jsonFormat7(Client)

  implicit val seqClientFormat = seqFormat[Client]
}

