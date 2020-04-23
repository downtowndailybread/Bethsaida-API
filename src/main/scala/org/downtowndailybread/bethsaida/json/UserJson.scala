package org.downtowndailybread.bethsaida.json

import java.time.format.DateTimeFormatter

import org.downtowndailybread.bethsaida.model.parameters._
import org.downtowndailybread.bethsaida.model.{ConfirmEmail, InternalUser}
import spray.json.DefaultJsonProtocol._
import spray.json._

trait UserJson extends BaseSupport {

  implicit val loginParametersFormat = jsonFormat2(LoginParameters)
  implicit val userCreateParameters = new RootJsonFormat[UserParameters] {
    override def read(json: JsValue): UserParameters = {
      (json: @unchecked) match {
        case JsObject(j) => UserParameters(
          j("firstName").convertTo[String],
          j("lastName").convertTo[String],
          LoginParameters(
            j("email").convertTo[String],
            j("password").convertTo[String]
          ),
          j.get("admin").map(_.convertTo[Boolean]).orElse(None)
        )
      }
    }

    override def write(obj: UserParameters): JsValue = {
      JsObject(Map(
        ("firstName", JsString(obj.firstName)),
        ("lastName", JsString(obj.lastName)),
        ("email", JsString(obj.loginParameters.email))
      ))
    }
  }

  implicit val confirmEmailFormat = jsonFormat3(ConfirmEmail)

  implicit val passwordResetFormat = jsonFormat3(PasswordResetParameters)

  implicit val initiatePasswordResetParameters = jsonFormat1(InitiatePasswordResetParameters)

  implicit val simpleUserFormat = new RootJsonFormat[InternalUser] {
    override def write(obj: InternalUser): JsValue = JsObject(
      Map(
        "id" -> JsString(obj.id),
        "firstName" -> JsString(obj.firstName),
        "lastName" -> JsString(obj.lastName),
        "email" -> JsString(obj.email),
        "admin" -> JsBoolean(obj.admin),
        "createTime" -> JsString(obj.createTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
        "latestActivity" -> JsString(obj.latestActivity.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
        "confirmed" -> JsBoolean(obj.confirmed),
        "adminLock" -> JsBoolean(obj.adminLock),
        "userLock" -> JsBoolean(obj.userLock)
      )
    )

    override def read(json: JsValue): InternalUser = ???
  }

  implicit val simpleUserSeqFormat = seqFormat[InternalUser]
}
