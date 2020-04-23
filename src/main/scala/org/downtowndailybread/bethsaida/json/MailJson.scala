package org.downtowndailybread.bethsaida.json

import java.util.UUID

import org.downtowndailybread.bethsaida.model.{Locker, LockerDetails, Mail, MailDetails}
import org.downtowndailybread.bethsaida.providers.SettingsProvider
import spray.json.DefaultJsonProtocol._
import spray.json.{JsObject, JsString, JsValue, RootJsonFormat}

trait MailJson extends BaseSupport {
    this: SettingsProvider =>

  implicit val mailDetailJson = jsonFormat4(MailDetails)

  implicit val mailJson = new RootJsonFormat[Mail] {
    override def read(json: JsValue): Mail = {
      val details = mailDetailJson.read(json)
      val id = json.asJsObject.fields("id").convertTo[UUID]
      Mail(id, details)
    }

    override def write(obj: Mail): JsValue = {
      JsObject(mailDetailJson.write(obj.mailDetails).asJsObject.fields ++ Map("id" -> JsString(obj.id)))
    }

    implicit val lockerSeqJson = seqFormat[MailDetails]
  }
}
