package org.downtowndailybread.model

//object ClientAttributeType extends Enumeration {
//
//  type ClientAttributeType = Value
//  val name = Value
//
//}
import scala.reflect.runtime.universe._


object ClientAttributeTypes {
  val attributes = List(
    Name,
    DateOfBirth
  )
}


sealed abstract class ClientAttributeType(val dataType: String, val required: Boolean) {
  val name = this.getClass.getSimpleName.replace("$", "").toLowerCase
}

case object Name extends ClientAttributeType("string", true)
case object DateOfBirth extends ClientAttributeType("date", true)