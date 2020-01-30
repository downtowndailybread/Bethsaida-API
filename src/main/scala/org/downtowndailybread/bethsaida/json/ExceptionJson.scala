package org.downtowndailybread.bethsaida.json

import org.downtowndailybread.bethsaida.exception.DDBException
import spray.json.{JsObject, JsString, JsValue, RootJsonWriter}

trait ExceptionJson {
  implicit val ddbExceptionFormat = new RootJsonWriter[DDBException] {
    override def write(obj: DDBException): JsValue = {
      JsObject(
        ("error", JsString(obj.errorType)),
        ("message", JsString(obj.getMessage))
      )
    }
  }
}
