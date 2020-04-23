package org.downtowndailybread.bethsaida.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.downtowndailybread.bethsaida.model._
import org.downtowndailybread.bethsaida.providers.{DatabaseConnectionProvider, SettingsProvider}
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonWriter}

trait JsonSupport
  extends SprayJsonSupport
    with DefaultJsonProtocol
    with SettingsProvider

    with DatabaseConnectionProvider

    with AttendanceJson
    with ClientJson
    with EventJson
    with ExceptionJson
    with LockerJson
    with MailJson
    with NoteJson
    with ServiceJson
    with StatsJson
    with UserJson {


  implicit val metadataFormat = jsonFormat1(Metadata)

  implicit val successFormat = new RootJsonWriter[Success] {
    override def write(obj: Success): JsValue = {
      JsObject(
        ("status", JsString("success")),
        ("message", JsString(obj.message))
      )
    }
  }

}
