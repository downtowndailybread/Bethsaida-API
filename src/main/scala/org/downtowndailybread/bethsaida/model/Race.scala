package org.downtowndailybread.bethsaida.model

import spray.json._

sealed abstract class Race(val idx: Int, val string: String)

case object White extends Race(0, "white")
case object NonWhite extends Race(1, "nonwhite")

object Race {

  private val all = List(White, NonWhite)

  def apply(str: String): Race = all.find(_.string == str).get

  implicit val raceFormat = new RootJsonFormat[Race] {
    override def read(json: JsValue): Race = {
      json match {
        case JsNumber(num) =>
          val idx = num.toInt
          idx match {
            case 0 => White
            case 1 => NonWhite
          }
      }
    }

    override def write(obj: Race): JsValue = {
      JsNumber(obj.idx)
    }
  }
}