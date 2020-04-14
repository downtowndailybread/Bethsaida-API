package org.downtowndailybread.bethsaida.json

import org.downtowndailybread.bethsaida.model.{Event, EventAttribute, HoursOfOperation}
import spray.json.DefaultJsonProtocol._
import spray.json._

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
