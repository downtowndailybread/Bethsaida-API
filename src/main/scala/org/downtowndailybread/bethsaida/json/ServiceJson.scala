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
          ServiceType.withName(o("type").convertTo[String]),
          o.get("defaultCapacity").map(r => r.convertTo[Int])
        )
      }
    }

    override def write(sa: ServiceAttributes): JsValue = {
      JsObject(
        ("name", JsString(sa.name)),
        ("type", JsString(sa.serviceType.toString)),
        ("defaultCapacity", sa.defaultCapacity match {
          case Some(cap) => JsNumber(cap)
          case None => JsNull
        })
      )
    }
  }

  implicit val scheduleDetailFormat = new RootJsonFormat[ScheduleDetail] {
    override def write(obj: ScheduleDetail): JsValue = {
      JsObject(
        ("rrule", obj.rrule),
        ("beginTime", localTimeFormat.write(obj.startTime)),
        ("endTime", localTimeFormat.write(obj.endTime)),
        ("capacity", obj.scheduleCapacity match {
          case Some(cap) => JsNumber(cap)
          case None => JsNull
        }),
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
            ob.get("capacity").map(_.convertTo[Int]),
            ob("enabled").convertTo[Boolean]
          )
      }
    }
  }

  implicit val scheduleFormat = jsonFormat3(Schedule)

  implicit val serviceFormat = jsonFormat3(Service)

  implicit val serviceSeqFormat = seqFormat[Service]

}
