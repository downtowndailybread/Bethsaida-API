package org.downtowndailybread.bethsaida.json

import java.time.LocalTime

import spray.json._
import DefaultJsonProtocol._
import org.downtowndailybread.bethsaida.model.{Schedule, ScheduleDetail, Service, ServiceAttributes}

trait ServiceJson extends BaseSupport {
//
//  implicit val serviceTypeFormat = new RootJsonFormat[ServiceType] {
//    override def read(json: JsValue): ServiceType = {
//      json match {
//        case JsString(s) => ServiceType.withName(s.toUpperCase())
//      }
//    }
//
//    override def write(obj: ServiceType): JsValue = {
//      JsString(obj.toString)
//    }
//  }


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

  implicit val serviceAttributes = jsonFormat3(ServiceAttributes)

  implicit val scheduleFormat = jsonFormat3(Schedule)

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
