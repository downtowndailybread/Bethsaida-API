package org.downtowndailybread.bethsaida.json

import java.time.LocalTime

import spray.json._
import DefaultJsonProtocol._
import org.downtowndailybread.bethsaida.model.{Schedule, ScheduleDetail, Service, ServiceAttributes, ServiceType}

trait ServiceJson extends BaseSupport {

  implicit val serviceAttributeFormat = new RootJsonFormat[ServiceAttributes] {
    override def read(json: JsValue): ServiceAttributes = {
      (json: @unchecked) match {
        case JsObject(o) => ServiceAttributes(
          o("name").convertTo[String],
          ServiceType.withName(o("type").convertTo[String])
        )
      }
    }

    override def write(sa: ServiceAttributes): JsValue = {
      JsObject(
        ("name", JsString(sa.name)),
        ("type", JsString(sa.serviceType.toString))
      )
    }
  }

  implicit val scheduleDetailFormat = new RootJsonFormat[ScheduleDetail] {
    override def write(obj: ScheduleDetail): JsValue = {
      JsObject(
        ("rrule", obj.rrule),
        ("beginTime", localTimeFormat.write(obj.beginTime)),
        ("endTime", localTimeFormat.write(obj.endTime)),
        ("enabled", JsBoolean(obj.enabled))
      )
    }

    override def read(json: JsValue): ScheduleDetail = {
      (json: @unchecked) match {
        case JsObject(ob) =>
          ScheduleDetail(
            ob("rrule").convertTo[String],
            ob("beginTime").convertTo[LocalTime],
            ob("endTime").convertTo[LocalTime],
            ob("enabled").convertTo[Boolean]
          )
      }
    }
  }

  implicit val scheduleFormat = jsonFormat2(Schedule)

  implicit val serviceFormat = jsonFormat3(Service)

  implicit val serviceSeqFormat = seqFormat[Service]

}
