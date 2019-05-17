package org.downtowndailybread.bethsaida.json

import spray.json._
import DefaultJsonProtocol._
import org.downtowndailybread.bethsaida.model.{Event, EventAttribute, HoursOfOperation}

trait EventJson extends BaseSupport {

  implicit val hoursOfOperationFormat = jsonFormat2(HoursOfOperation)
  implicit val eventAttributeFormat = jsonFormat4(EventAttribute)
  implicit val eventFormat = jsonFormat3(Event)

  implicit val seqEventFormat = seqFormat[Event]
}
