package org.downtowndailybread.bethsaida.model

import spray.json._

sealed abstract class Race(val idx: Int, val string: String)

/*
•	African American or Black
•	Asian
•	Caucasian
•	Native American
•	Other
•	Client Refused to Identify



 */

case object Black extends Race(0, "black")
case object Asian extends Race(1, "asian")
case object Caucasian extends Race(2, "caucasian")
case object NativeAmerican extends Race(3, "native_american")
case object PacificIslander extends Race(4, "pacific_islander")
case object OtherRace extends Race(5, "other")
case object Refused extends Race(6, "refused_to_identify")
case object NotApplicable extends Race(7, "not_applicable")

object Race {

  private val all = List(Black, Asian, Caucasian, NativeAmerican, PacificIslander, OtherRace, Refused, NotApplicable)

  def apply(str: String): Race = all.find(_.string == str).getOrElse(throw new Exception(s"could not find race $str"))

  implicit val raceFormat = new RootJsonFormat[Race] {
    override def read(json: JsValue): Race = {
      json match {
        case JsNumber(num) =>
          val idx = num.toInt
          all.find(_.idx == idx).get
      }
    }

    override def write(obj: Race): JsValue = {
      JsNumber(obj.idx)
    }
  }
}