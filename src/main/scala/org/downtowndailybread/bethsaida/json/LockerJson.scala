package org.downtowndailybread.bethsaida.json

import java.util.UUID

import org.downtowndailybread.bethsaida.model.{Locker, LockerDetails}
import org.downtowndailybread.bethsaida.providers.SettingsProvider
import spray.json.DefaultJsonProtocol._
import spray.json.{JsObject, JsString, JsValue, RootJsonFormat}

trait LockerJson extends BaseSupport {
    this: SettingsProvider =>

  implicit val lockerDetailJson = jsonFormat6(LockerDetails)

  implicit val lockerJson = new RootJsonFormat[Locker] {
    override def read(json: JsValue): Locker = {
      val details = lockerDetailJson.read(json)
      val id = json.asJsObject.fields("id").convertTo[UUID]
      Locker(id, details)
    }

    override def write(obj: Locker): JsValue = {
      JsObject(lockerDetailJson.write(obj.lockerDetails).asJsObject.fields ++ Map("id" -> JsString(obj.id)))
    }

    implicit val lockerSeqJson = seqFormat[LockerDetails]
  }
}
