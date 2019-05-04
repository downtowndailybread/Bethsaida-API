package org.downtowndailybread.bethsaida.request

import java.sql.Connection

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.auth.{PasswordDoesNotMatchException, UserAccountLockedByAdminException, UserAccountLockedException, UserAccountNotConfirmedException}
import org.downtowndailybread.bethsaida.exception.user.UserNotFoundException
import org.downtowndailybread.bethsaida.model.parameters.LoginParameters
import org.downtowndailybread.bethsaida.model.{ConfirmEmail, InternalUser}
import org.downtowndailybread.bethsaida.request.util.BaseRequest
import org.downtowndailybread.bethsaida.service.{HashProvider, UUIDProvider}

class AuthRequest(settings: Settings, conn: Connection) extends BaseRequest with HashProvider with UUIDProvider {

  def getUser(userParameters: LoginParameters): InternalUser = {
    val userRequest = new UserRequest(conn)
    val user = userRequest.getRawUserFromEmailOptional(userParameters.email)
    user match {
      case Some(u) if u.hash != hashPassword(userParameters.password, u.salt) =>
        throw new PasswordDoesNotMatchException
      case Some(u) if !u.confirmed =>
        throw new UserAccountNotConfirmedException
      case Some(u) if u.userLock =>
        throw new UserAccountLockedException
      case Some(u) if u.adminLock =>
        throw new UserAccountLockedByAdminException
      case Some(u) => u
    }
  }

  def confirmUser(emailConfirm: ConfirmEmail)(implicit au: InternalUser): Unit = {
    new UserRequest(conn).confirmEmail(emailConfirm.email, emailConfirm.token)
  }
}
