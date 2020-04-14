package org.downtowndailybread.bethsaida.request

import java.sql.Connection
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.auth._
import org.downtowndailybread.bethsaida.exception.user.UserNotFoundException
import org.downtowndailybread.bethsaida.model.parameters.LoginParameters
import org.downtowndailybread.bethsaida.model.{ConfirmEmail, InternalUser}
import org.downtowndailybread.bethsaida.providers.{HashProvider, UUIDProvider}
import org.downtowndailybread.bethsaida.request.util.BaseRequest

class AuthRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with HashProvider
    with UUIDProvider {

  /**
    * Finds a user in the database based on the login parameters given. This method does all of the validation of
    * password, active checks, and locked checks.
    *
    * @param loginParameters The login parameters
    * @return the user if the parameters correctly match to a user in the database, or throws an exception.
    */
  def getUser(loginParameters: LoginParameters): InternalUser = {
    val userRequest = new UserRequest(settings, conn)
    val user = userRequest.getRawUserFromEmailOptional(loginParameters.email)
    user match {
      case Some(u) if u.hash != hashPassword(loginParameters.password, u.salt) =>
        throw new PasswordDoesNotMatchException
      case Some(u) if !u.confirmed =>
        throw new UserAccountNotConfirmedException
      case Some(u) if u.userLock =>
        throw new UserAccountLockedException
      case Some(u) if u.adminLock =>
        throw new UserAccountLockedByAdminException
      case Some(u) => u
      case None => throw new UserNotFoundException
    }
  }

  /**
    * This action "confirms" the user, meaning the email address is associated with the user because they clicked on a
    * link in the email.
    *
    * @param emailConfirm the object associating an email and a token.
    */
  def confirmUser(emailConfirm: ConfirmEmail): UUID = {
    new UserRequest(settings, conn).confirmEmail(emailConfirm)
  }
}
