package org.downtowndailybread.bethsaida.model

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import java.util.UUID

import org.downtowndailybread.bethsaida.model.parameters.{LoginParameters, UserParameters}
import spray.json.{JsNull, JsObject, JsString, JsValue, RootJsonFormat}

case class InternalUser(
                         id: UUID,
                         email: String,
                         firstName: String,
                         lastName: String,
                         salt: String,
                         hash: String,
                         confirmed: Boolean,
                         resetToken: Option[UUID],
                         userLock: Boolean,
                         adminLock: Boolean,
                         admin: Boolean,
                         createTime: ZonedDateTime,
                         latestActivity: ZonedDateTime
                       ) {
  def getUserParameters(withPassword: String): UserParameters = UserParameters(
    firstName,
    lastName,
    LoginParameters(email, withPassword),
    Some(admin)
  )
}

object InternalUser {
  implicit val converter = new RootJsonFormat[Option[InternalUser]] {
    override def write(o: Option[InternalUser]): JsValue = {
      o match {
        case Some(obj) =>
          JsObject(
            Map(
              "id" -> JsString(obj.id.toString),
              "firstName" -> JsString(obj.firstName),
              "lastName" -> JsString(obj.lastName)
            )
          )
        case None => JsNull
      }

    }

    override def read(json: JsValue): Option[InternalUser] = None
  }
}

object AnonymousUser extends InternalUser(
  UUID.fromString("00000000-0000-0000-0000-000000000000"),
  "", "", "", "", "", true, None, false, false, false,
  ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("America/New_York")), ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("America/New_York")))
