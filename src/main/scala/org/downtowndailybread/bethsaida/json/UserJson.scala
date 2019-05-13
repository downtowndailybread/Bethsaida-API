package org.downtowndailybread.bethsaida.json

import spray.json._
import DefaultJsonProtocol._
import org.downtowndailybread.bethsaida.model.parameters._
import org.downtowndailybread.bethsaida.model.{ConfirmEmail, InternalUser}

trait UserJson extends BaseSupport {

  implicit val loginParametersFormat = jsonFormat2(LoginParameters)
  implicit val userCreateParameters = new RootJsonFormat[UserParameters] {
    override def read(json: JsValue): UserParameters = {
      (json: @unchecked) match {
        case JsObject(j) => UserParameters(
          j("name").convertTo[String],
          LoginParameters(
            j("email").convertTo[String],
            j("password").convertTo[String]
          )
        )
      }
    }

    override def write(obj: UserParameters): JsValue = {
      JsObject(Map(
        ("name", JsString(obj.name)),
        ("email", JsString(obj.loginParameters.email))
      ))
    }
  }

  implicit val confirmEmailFormat = jsonFormat2(ConfirmEmail)

  implicit val internalUserFormat = new RootJsonFormat[InternalUser] {
    override def write(obj: InternalUser): JsValue = {
      JsObject(
        ("id", JsString(obj.id)),
        ("name", JsString(obj.name)),
        ("email", JsString(obj.email)),
        ("userLock", JsBoolean(obj.userLock)),
        ("adminLock", JsBoolean(obj.adminLock)),
        ("confirmed", JsBoolean(obj.confirmed)),
        ("resetToken", obj.resetToken.map(_.toString))
      )
    }

    override def read(json: JsValue): InternalUser = {
      json match {
        case JsObject(o) =>
          InternalUser(
            parseUUID(o("id").convertTo[String]),
            o("email").convertTo[String],
            o("name").convertTo[String],
            "",
            "",
            o("confirmed").convertTo[Boolean],
            o.get("resetToken").flatMap(r => r match {
              case JsString(s) => Some(s)
              case JsNull => None
            } ).map(parseUUID),
            o("userLock").convertTo[Boolean],
            o("adminLock").convertTo[Boolean]
          )
      }
    }
  }

  implicit val userSeqFormat = seqFormat[InternalUser]

  implicit val passwordResetFormat = jsonFormat3(PasswordResetParameters)

  implicit val initiatePasswordResetParameters = jsonFormat1(InitiatePasswordResetParameters)
}
