package org.downtowndailybread.json


import spray.json._
import DefaultJsonProtocol._
import org.downtowndailybread.model.{ConfirmEmail, InternalUser, LoginParameters, NewUserParameters}

trait UserJson extends BaseSupport {

  implicit val loginParametersFormat = jsonFormat2(LoginParameters)
  implicit val userCreateParameters = new RootJsonFormat[NewUserParameters] {
    override def read(json: JsValue): NewUserParameters = {
      json match {
        case JsObject(j) => NewUserParameters(
          j("name").convertTo[String],
          LoginParameters(
            j("email").convertTo[String],
            j("password").convertTo[String]
          )
        )
      }
    }

    override def write(obj: NewUserParameters): JsValue = {
      JsObject(Map(
        ("name", JsString(obj.name)),
        ("email", JsString(obj.loginParameters.email))
      ))
    }
  }

  implicit val confirmEmailFormat = jsonFormat2(ConfirmEmail)
}
