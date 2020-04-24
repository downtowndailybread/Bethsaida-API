package org.downtowndailybread.bethsaida.json

import org.downtowndailybread.bethsaida.model.{Service, ServiceAttributes, ServiceType, ServiceTypeObj}
import spray.json.DefaultJsonProtocol._
import spray.json._

trait ServiceJson extends BaseSupport {

  implicit val serviceTypeAttrib = new RootJsonFormat[ServiceType] {
    override def write(obj: ServiceType): JsValue = JsNumber(obj.idx)

    override def read(json: JsValue): ServiceType = ServiceTypeObj.serviceTypeFormat.read(json)
  }

  implicit val serviceAttributes = jsonFormat3(ServiceAttributes)

  implicit val serviceFormat = new RootJsonFormat[Service] {
    override def write(obj: Service): JsValue = {
      JsObject(mapStringConverter(Map(
        "id" -> obj.id.toString,
        "name" -> obj.attributes.name
      )) ++ Map("serviceType" -> JsNumber(obj.attributes.serviceType.idx))
        ++ (if (obj.attributes.defaultCapacity.isDefined) {
        Map("maxCapacity" -> JsNumber(obj.attributes.defaultCapacity.get))
      } else {
        Map()
      }))
    }

    override def read(json: JsValue): Service = ???
  }

  implicit val serviceSeqFormat = seqFormat[Service]

}
