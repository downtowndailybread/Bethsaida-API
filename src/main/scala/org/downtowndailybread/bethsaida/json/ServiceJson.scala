package org.downtowndailybread.bethsaida.json

import spray.json._
import DefaultJsonProtocol._
import org.downtowndailybread.bethsaida.model.{Schedule, ScheduleDetail, Service, ServiceAttribute, ServiceType}

trait ServiceJson extends BaseSupport {

  implicit val serviceAttributeFormat = new RootJsonFormat[ServiceAttribute] {
    override def read(json: JsValue): ServiceAttribute = {
      (json: @unchecked) match {
        case JsObject(o) => ServiceAttribute(
          o("name").convertTo[String],
          ServiceType.withName(o("type").convertTo[String])
        )
      }
    }

    override def write(sa: ServiceAttribute): JsValue = {
      JsObject(
        ("name", JsString(sa.name)),
        ("type", JsString(sa.serviceType.toString))
      )
    }
  }
  implicit val scheduleDetailFormat = jsonFormat2(ScheduleDetail)
  implicit val scheduleFormat = jsonFormat2(Schedule)

  implicit val serviceFormat = jsonFormat3(Service)

  implicit val serviceSeqFormat = seqFormat[Service]

}
