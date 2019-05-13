package org.downtowndailybread.bethsaida.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.downtowndailybread.bethsaida.model._
import org.downtowndailybread.bethsaida.providers.SettingsProvider
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonWriter}

trait JsonSupport
  extends SprayJsonSupport
    with DefaultJsonProtocol
    with ClientJson
    with UserJson
    with ServiceJson
    with EventJson
    with SettingsProvider
    with ExceptionJson {


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
