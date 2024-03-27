package org.downtowndailybread.bethsaida.model

import spray.json._

sealed abstract class Gender(val idx: Int, val string: String)

case object Male extends Gender(0, "male")

case object Female extends Gender(1, "female")

case object OtherGender extends Gender(2, "other")

case object NonBinary extends Gender(3, "non_binary")

case object FemaleBornMale extends Gender(4, "f_born_m")

case object MaleBornFemale extends Gender(5, "m_born_f")

object Gender {

  private val all = List(Male, Female, OtherGender, NonBinary, FemaleBornMale, MaleBornFemale)

  def apply(str: String): Gender = all.find(_.string == str).get

  implicit val genderFormat = new RootJsonFormat[Gender] {
    override def read(json: JsValue): Gender = {
      json match {
        case JsNumber(num) =>
          val idx = num.toInt
          all.find(_.idx == idx).get
      }
    }

    override def write(obj: Gender): JsValue = {
      JsNumber(obj.idx)
    }
  }
}