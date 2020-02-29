package org.downtowndailybread.bethsaida.model

import spray.json.{JsNumber, JsValue, RootJsonFormat}

sealed abstract class ServiceType(val idx: Int, val string: String)


case object Recurring extends ServiceType(0, "recurring")
case object Single extends ServiceType(1, "single")



object ServiceType {

  private val all = List(Recurring, Single)

  def apply(str: String): ServiceType = all.find(_.string == str.toLowerCase).get

  implicit val serviceTypeFormat = new RootJsonFormat[ServiceType] {
    override def read(json: JsValue): ServiceType = {
      json match {
        case JsNumber(num) =>
          val idx = num.toInt
          idx match {
            case 0 => Recurring
            case 1 => Single
          }
      }
    }

    override def write(obj: ServiceType): JsValue = {
      JsNumber(obj.idx)
    }
  }
}