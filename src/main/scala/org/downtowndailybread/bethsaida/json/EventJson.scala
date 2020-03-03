package org.downtowndailybread.bethsaida.json

import java.util.UUID

import spray.json._
import DefaultJsonProtocol._
import org.downtowndailybread.bethsaida.model.{Event, EventAttribute, HoursOfOperation}

trait EventJson extends BaseSupport {

  implicit val hoursOfOperationFormat = jsonFormat2(HoursOfOperation)
  implicit val eventAttributeFormat = jsonFormat4(EventAttribute)
  implicit val eventFormat = new RootJsonFormat[Event] {
    override def write(obj: Event): JsValue = {
      JsObject(eventAttributeFormat.write(obj.attribute).asJsObject.fields ++ Map(
        "id" -> JsString(obj.id.toString),
        "creatorId" -> JsString(obj.attribute.createUserId.toString)
      ))
    }

    override def read(json: JsValue): Event = ???
  }

  implicit val seqEventFormat = seqFormat[Event]
}
