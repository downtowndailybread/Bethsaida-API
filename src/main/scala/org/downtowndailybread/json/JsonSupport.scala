package org.downtowndailybread.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.downtowndailybread.exceptions.DDBException
import org.downtowndailybread.model._
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonWriter}

trait JsonSupport
  extends SprayJsonSupport
    with DefaultJsonProtocol
    with ClientJson
//    with ServiceJson
//    with EventJson
{


  implicit val metadataFormat = jsonFormat1(Metadata)

  implicit val ddbExceptionFormat = new RootJsonWriter[DDBException] {
    override def write(obj: DDBException): JsValue = {
      JsObject(
        ("error", JsString(obj.errorType)),
        ("message", JsString(obj.getMessage))
      )
    }
  }

  implicit val successFormat = new RootJsonWriter[Success] {
    override def write(obj: Success): JsValue = {
      JsObject(
        ("status", JsString("success")),
        ("message", JsString(obj.message))
      )
    }
  }

}
