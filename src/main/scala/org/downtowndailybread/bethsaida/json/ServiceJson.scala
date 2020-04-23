package org.downtowndailybread.bethsaida.json

import org.downtowndailybread.bethsaida.model.{Service, ServiceAttributes}
import spray.json.DefaultJsonProtocol._
import spray.json._

trait ServiceJson extends BaseSupport {

  implicit val serviceAttributes = jsonFormat3(ServiceAttributes)

  implicit val serviceFormat = new RootJsonFormat[Service] {
    override def write(obj: Service): JsValue = {
      JsObject(mapStringConverter(Map(
        "id" -> obj.id.toString,
        "name" -> obj.attributes.name,
        "serviceType" -> obj.attributes.serviceType.toString
      )
      ) ++ (if (obj.attributes.defaultCapacity.isDefined) {
        Map("maxCapacity" -> JsNumber(obj.attributes.defaultCapacity.get))
      } else {
        Map()
      }))
    }

    override def read(json: JsValue): Service = ???
  }

  implicit val serviceSeqFormat = seqFormat[Service]

}
