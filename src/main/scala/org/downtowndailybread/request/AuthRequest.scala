package org.downtowndailybread.request

import java.sql.Connection

import org.downtowndailybread.exception.auth.{PasswordDoesNotMatchException, UserAccountLockedByAdminException, UserAccountLockedException, UserAccountNotConfirmedException}
import org.downtowndailybread.exception.user.UserNotFoundException
import org.downtowndailybread.model.{ConfirmEmail, InternalUser, LoginParameters}
import org.downtowndailybread.service.{HashProvider, UUIDProvider}

class AuthRequest(conn: Connection) extends HashProvider with UUIDProvider {

  def getUser(userParameters: LoginParameters): InternalUser = {
    val userRequest = new UserRequest(conn)
    val user = userRequest.getRawUserFromEmail(userParameters.email)

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
      case None => throw new UserNotFoundException
    }
  }

  def confirmUser(emailConfirm: ConfirmEmail): Unit = {
    val userRequest = new UserRequest(conn)
    val user = userRequest.getRawUserFromEmail(emailConfirm.email) match {
      case Some(u) => u
      case None => throw new UserNotFoundException
    }

    userRequest.insertLoginInfoRecord(user.copy(confirmed = true, resetToken = getUUID()))
  }
}
