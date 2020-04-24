package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet}
import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID

import com.sun.jna.platform.win32.WinNT.SYSTEM_LOGICAL_PROCESSOR_INFORMATION.AnonymousUnionPayload
import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.emailer.Emailer
import org.downtowndailybread.bethsaida.exception.user.{EmailAlreadyExistsException, UserNotFoundException}
import org.downtowndailybread.bethsaida.model.parameters.UserParameters
import org.downtowndailybread.bethsaida.model.{AnonymousUser, ConfirmEmail, InternalUser}
import org.downtowndailybread.bethsaida.providers.{HashProvider, UUIDProvider}
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}
import org.postgresql.util.PSQLException

import scala.util.Try

class UserRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider
    with HashProvider {

  def emailAndTokenMatch(email: String, confirmation: UUID): Option[InternalUser] = {
    val ret = getRawUserFromEmailOptional(email) match {
      case Some(user) if user.resetToken.contains(confirmation) =>
        Some(user)
      case _ => None
    }
    ret
  }

  def confirmEmail(confirmation: ConfirmEmail): UUID = {
    emailAndTokenMatch(confirmation.email, confirmation.token) match {
      case Some(user) => updateUserRecords(user.copy(confirmed = true, resetToken = None, hash = hashPassword(confirmation.password, user.salt)))(user)
        user.id
      case None => throw new UserNotFoundException
    }
  }

  def getAllUsers(): Seq[InternalUser] = {
    val ps = conn.prepareStatement(rawUserSql)

    createSeq(ps.executeQuery(), extractUserFromRs)
  }

  def getRawUserFromUuid(uuid: UUID): InternalUser = {
    val sql = rawUserSql + "\n and id = cast(? as uuid)"

    val ps = conn.prepareStatement(sql)
    ps.setString(1, uuid)

    extractSingleUserFromRs(ps.executeQuery())
  }

  def getRawUserFromEmail(email: String): InternalUser = {
    val sql = rawUserSql + "\n and email = ?"

    val ps = conn.prepareStatement(sql)
    ps.setString(1, email)

    extractSingleUserFromRs(ps.executeQuery())
  }

  def getRawUserFromEmailOptional(email: String): Option[InternalUser] = {
    Try(getRawUserFromEmail(email)).toOption
  }

  def updateUserFromReset(user: UserParameters)(implicit iu: InternalUser): UUID = {
    val ur  = getRawUserFromEmail(user.loginParameters.email)
    updateUserRecords(ur.copy(
      email = user.loginParameters.email,
      hash = hashPassword(user.loginParameters.password, ur.salt),
      firstName = user.firstName,
      lastName = user.lastName,
      resetToken = None
    ))
  }

  def updateUser(user: UserParameters, userId: UUID)(implicit iu: InternalUser): UUID = {
    val ur = getRawUserFromUuid(userId)
    val adminFlag = if(iu.admin) {
      user.admin.getOrElse(ur.admin)
    } else {
      ur.admin
    }
    updateUserRecords(ur.copy(
      email = user.loginParameters.email,
      hash = if(user.loginParameters.password.isBlank) ur.hash else hashPassword(user.loginParameters.password, ur.salt),
      firstName = user.firstName,
      lastName = user.lastName,
      admin = adminFlag
    ))
  }

  def deleteUser(user: UserParameters)(implicit iu: InternalUser): UUID = {
    val ur = getRawUserFromEmail(user.loginParameters.email)
    updateUserRecords(ur)
  }

  def  insertUser(user: UserParameters)(implicit au: InternalUser): UUID = {
    val userId = getUUID()
    val createBaseRecordSql =
      s"""
         |insert into user_account
         |    (id, email, firstName, lastName, salt, hash, confirmed, admin_lock, user_lock, reset_token, admin)
         |VALUES
         |    (cast(? as uuid), ?, ?, ?, ?, ?, ?, ?, ?, cast(? as uuid), ?)
       """.stripMargin
    val salt = generateSalt()
    val hash = hashPassword(user.loginParameters.password, salt)

    val ps = conn.prepareStatement(createBaseRecordSql)
    ps.setString(1, userId)
    ps.setString(2, user.loginParameters.email)
    ps.setString(4, user.firstName)
    ps.setString(3, user.lastName)
    ps.setString(5, salt)
    ps.setString(6, hash)
    ps.setBoolean(7, false)
    ps.setBoolean(8, false)
    ps.setBoolean(9, false)
    ps.setNullableString(10, Some(getUUID()))
    ps.setBoolean(11, user.admin.exists(identity))
    try {
      ps.executeUpdate()
    } catch {
      case e: PSQLException if e.getServerErrorMessage.getConstraint == "user_account_email_key" =>
        throw new EmailAlreadyExistsException(user.loginParameters.email)
    }

    userId
  }

  def initiatePasswordReset(email: String): Unit = {
    val user = getRawUserFromEmail(email).copy(resetToken = Some(getUUID()))
    updateUserRecords(user)(AnonymousUser)

    Emailer.sendEmail(
      user.email,
      "Reset your Bethsaida password",
      "Please click the following link to complete your signup.\n\n" +
        s"https://${if(settings.isDev) "edge." else ""}bethsaida.downtowndailybread.org/confirm/${user.email}/${user.resetToken.getOrElse("")}",
      settings
    )

  }

  private def updateUserRecords(t: InternalUser)(implicit iu: InternalUser): UUID = {
    val sql =
      s"""
         |update user_account
         |  set email = ?,
         |      first_name = ?,
         |      last_name = ?,
         |      salt = ?,
         |      hash = ?,
         |      confirmed = ?,
         |      admin_lock = ?,
         |      user_lock = ?,
         |      reset_token = cast(? as uuid),
         |      admin = ?
         |where id = cast(? as uuid)
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, t.email)
    ps.setString(2, t.firstName)
    ps.setString(3, t.lastName)
    ps.setString(4, t.salt)
    ps.setString(5, t.hash)
    ps.setBoolean(6, t.confirmed)
    ps.setBoolean(7, t.adminLock)
    ps.setBoolean(8, t.userLock)
    ps.setNullableUUID(9, t.resetToken)
    ps.setBoolean(10, t.admin)
    ps.setString(11, t.id)

    ps.executeUpdate()
    t.id
  }

  def touchTimestamp(id: UUID): Unit = {
    val sql =
      s"""
      update user_account
      set latest_activity = now()
      where id = cast(? as uuid)
         """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, id.toString)
    ps.executeUpdate()
  }

  private def extractSingleUserFromRs(resultSet: ResultSet): InternalUser = {
    if (resultSet.next())
      extractUserFromRs(resultSet)
    else
      throw new UserNotFoundException
  }

  private def extractUserFromRs(resultSet: ResultSet): InternalUser = {
    InternalUser(
      resultSet.getString("id"),
      resultSet.getString("email"),
      resultSet.getString("first_name"),
      resultSet.getString("last_name"),
      resultSet.getString("salt"),
      resultSet.getString("hash"),
      resultSet.getBoolean("confirmed"),
      resultSet.getOptionalUUID("reset_token"),
      resultSet.getBoolean("user_lock"),
      resultSet.getBoolean("admin_lock"),
      resultSet.getBoolean("admin"),
      ZonedDateTime.of(resultSet.getTimestamp("create_time").toLocalDateTime, ZoneId.of("America/New_York")),
      ZonedDateTime.of(resultSet.getTimestamp("latest_activity").toLocalDateTime, ZoneId.of("America/New_York"))
    )
  }


  private lazy val rawUserSql =
    s"""
       |select id, email, first_name, last_name, salt, hash, confirmed, reset_token, user_lock, admin_lock, admin, create_time, latest_activity
       |from user_account
       |where 1=1
     """.stripMargin
}
