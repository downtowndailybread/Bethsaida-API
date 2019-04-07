package org.downtowndailybread.model.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.downtowndailybread.model.exceptions.DDBException
import org.downtowndailybread.model._
import spray.json.{DefaultJsonProtocol, JsArray, JsBoolean, JsNumber, JsObject, JsString, JsValue, RootJsonWriter}

trait JsonSupport
  extends SprayJsonSupport
    with DefaultJsonProtocol
    with ClientJson
    with ServiceJson
    with EventJson
{


  implicit val metadataFormat = jsonFormat1(Metadata)

  implicit val ddbExceptionFormat = new RootJsonWriter[DDBException] {
    override def write(obj: DDBException): JsValue = {
      JsObject(
        ("error", JsString(obj.getClass.getSimpleName)),
        ("message", JsString(obj.getMessage))
      )
    }
  }

}
