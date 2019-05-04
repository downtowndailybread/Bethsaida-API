package org.downtowndailybread.bethsaida.json

import spray.json._
import DefaultJsonProtocol._
import org.downtowndailybread.bethsaida.model.parameters.{LoginParameters, PasswordResetParameters, UserParameters}
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

  implicit val internalUserFormat = new RootJsonWriter[InternalUser] {
    override def write(obj: InternalUser): JsValue = {
      JsObject(
        ("id", JsString(obj.id)),
        ("name", JsString(obj.name)),
        ("email", JsString(obj.email)),
        ("userLock", JsBoolean(obj.userLock)),
        ("adminLock", JsBoolean(obj.adminLock)),
        ("confirmed", JsBoolean(obj.confirmed)),
        ("resetToken", JsString(obj.resetToken.map(_.toString).getOrElse("")))
      )
    }
  }

  implicit val userSeqFormat = seqWriter[InternalUser]

  implicit val passwordResetFormat = jsonFormat3(PasswordResetParameters)
}
