package org.downtowndailybread.model.json

import org.downtowndailybread.model.Service
import spray.json.{JsArray, JsBoolean, JsNumber, JsObject, JsString, JsValue, RootJsonWriter}

trait ServiceJson {
  implicit val serviceFormat = new RootJsonWriter[Service] {
    override def write(service: Service): JsValue = {
      JsObject(
        ("id", JsNumber(service.metadata.id)),
        ("name", JsString(service.name))
      )
    }
  }

  implicit val serviceSeqFormat = new RootJsonWriter[Seq[Service]] {
    override def write(services: Seq[Service]): JsValue = {
      JsArray(
        services.map(serviceFormat.write).toVector
      )
    }
  }
}
