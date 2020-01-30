package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet}
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.user.{EmailAlreadyExistsException, UserNotFoundException}
import org.downtowndailybread.bethsaida.model.parameters.UserParameters
import org.downtowndailybread.bethsaida.model.InternalUser
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}
import org.downtowndailybread.bethsaida.providers.{HashProvider, UUIDProvider}
import org.postgresql.util.PSQLException

import scala.util.Try

class UserRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider
    with HashProvider {

  def emailAndTokenMatch(email: String, confirmation: UUID): Option[InternalUser] = {
    getRawUserFromEmailOptional(email) match {
      case Some(user) if user.resetToken.contains(confirmation) =>
        Some(user)
      case _ => None
    }
  }

  def confirmEmail(email: String, confirmation: UUID)(implicit au: InternalUser): Unit = {
    emailAndTokenMatch(email, confirmation) match {
      case Some(user) => updateUserRecords(user.copy(confirmed = true, resetToken = None))
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
      name = user.name,
      resetToken = None
    ))
  }

  def updateUser(user: UserParameters, userId: UUID)(implicit iu: InternalUser): UUID = {
    val ur = getRawUserFromUuid(userId)
    updateUserRecords(ur.copy(
      email = user.loginParameters.email,
      hash = hashPassword(user.loginParameters.password, ur.salt),
      name = user.name
    ))
  }

  def deleteUser(user: UserParameters)(implicit iu: InternalUser): UUID = {
    val ur = getRawUserFromEmail(user.loginParameters.email)
    updateUserRecords(ur)
  }

  def insertUser(user: UserParameters)(implicit au: InternalUser): UUID = {
    val baseMetaId = insertMetadataStatement(conn, true)
    val userId = getUUID()
    val createBaseRecordSql =
      s"""
         |insert into user_account
         |    (id, email, name, salt, hash, confirmed, admin_lock, user_lock, reset_token, metadata_id)
         |VALUES
         |    (cast(? as uuid), ?, ?, ?, ?, ?, ?, ?, cast(? as uuid), ?)
       """.stripMargin
    val salt = generateSalt()
    val hash = hashPassword(user.loginParameters.password, salt)

    val ps = conn.prepareStatement(createBaseRecordSql)
    ps.setString(1, userId)
    ps.setString(2, user.loginParameters.email)
    ps.setString(3, user.name)
    ps.setString(4, salt)
    ps.setString(5, hash)
    ps.setBoolean(6, false)
    ps.setBoolean(7, false)
    ps.setBoolean(8, false)
    ps.setNullableString(9, Some(getUUID()))
    ps.setInt(10, baseMetaId)
    try {
      ps.executeUpdate()
    } catch {
      case e: PSQLException if e.getServerErrorMessage.getConstraint == "user_account_email_key" =>
        throw new EmailAlreadyExistsException(user.loginParameters.email)
    }

    userId
  }

  def initiatePasswordReset(email: String)(implicit iu: InternalUser): Unit = {
    val user = getRawUserFromEmail(email)
    updateUserRecords(user.copy(resetToken = Some(getUUID())))
  }

  private def updateUserRecords(t: InternalUser)(implicit iu: InternalUser): UUID = {
    val sql =
      s"""
         |update user_account
         |  set email = ?,
         |      name = ?,
         |      salt = ?,
         |      hash = ?,
         |      confirmed = ?,
         |      admin_lock = ?,
         |      user_lock = ?,
         |      reset_token = cast(? as uuid)
         |where id = cast(? as uuid)
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, t.email)
    ps.setString(2, t.name)
    ps.setString(3, t.salt)
    ps.setString(4, t.hash)
    ps.setBoolean(5, t.confirmed)
    ps.setBoolean(6, t.adminLock)
    ps.setBoolean(7, t.userLock)
    ps.setNullableUUID(8, t.resetToken)
    ps.setString(9, t.id)
    ps.executeUpdate()
    t.id
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
      resultSet.getString("name"),
      resultSet.getString("salt"),
      resultSet.getString("hash"),
      resultSet.getBoolean("confirmed"),
      resultSet.getOptionalUUID("reset_token"),
      resultSet.getBoolean("user_lock"),
      resultSet.getBoolean("admin_lock")
    )
  }


  private lazy val rawUserSql =
    s"""
       |select id, email, name, salt, hash, confirmed, reset_token, user_lock, admin_lock
       |from user_account
       |where 1=1
     """.stripMargin
}
