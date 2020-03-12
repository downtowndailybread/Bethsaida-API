package org.downtowndailybread.bethsaida.json

import java.time.ZonedDateTime
import java.util.UUID

import spray.json._
import DefaultJsonProtocol._
import org.downtowndailybread.bethsaida.model.{Attendance, AttendanceAttribute}
import org.downtowndailybread.bethsaida.providers.SettingsProvider
import spray.json.{JsObject, JsValue, RootJsonFormat}

trait AttendanceJson extends BaseSupport {
    this: SettingsProvider =>



  implicit val attendanceAttributeJsonConverter = new RootJsonFormat[AttendanceAttribute] {
    override def read(json: JsValue): AttendanceAttribute = {
      val jo = json.asJsObject.fields
      AttendanceAttribute(
        jo("eventId").convertTo[UUID],
        jo("clientId").convertTo[UUID],
        jo("checkInTime").convertTo[ZonedDateTime]
      )
    }

    override def write(obj: AttendanceAttribute): JsValue = {
      new JsObject(
        Map(
          "eventId" -> uuidFormat.write(obj.eventId),
          "clientId" -> uuidFormat.write(obj.clientId),
          "checkInTime" -> zonedDateTimeFormat.write(obj.checkInTime)
        )
      )
    }
  }

  implicit val attendanceJson = new RootJsonFormat[Attendance] {
    override def read(json: JsValue): Attendance = {
      val attrib = attendanceAttributeJsonConverter.read(json)
      val id = json.asJsObject.fields("id").convertTo[String]
      Attendance(
        id,
        attrib
      )
    }

    override def write(obj: Attendance): JsValue = {
      JsObject(attendanceAttributeJsonConverter
        .write(obj.attribute).asJsObject.fields ++ Map("id" -> uuidFormat.write(obj.id)))
    }
  }

  implicit val attendanceSeqJsonConverter = seqFormat[AttendanceAttribute]
}
