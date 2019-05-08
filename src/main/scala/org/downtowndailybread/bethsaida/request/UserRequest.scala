package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet}
import java.util.UUID

import org.downtowndailybread.bethsaida.exception.user.{EmailAlreadyExistsException, UserNotFoundException}
import org.downtowndailybread.bethsaida.model.parameters.UserParameters
import org.downtowndailybread.bethsaida.model.{AnonymousUser, InternalUser}
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}
import org.downtowndailybread.bethsaida.service.{HashProvider, UUIDProvider}

import scala.util.Try

class UserRequest(val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider
    with HashProvider {

  def getAnonymousUser(): InternalUser = AnonymousUser

  def emailAndTokenMatch(email: String, confirmation: UUID): Option[InternalUser] = {
    getRawUserFromEmailOptional(email) match {
      case Some(user) if !user.confirmed && user.resetToken.contains(confirmation) =>
        Some(user)
      case _ => None
    }
  }

  def confirmEmail(email: String, confirmation: UUID)(implicit au: InternalUser): Unit = {
    emailAndTokenMatch(email, confirmation) match {
      case Some(user) => updateUserRecords(user.copy(confirmed = true, resetToken = None), true)
      case None =>
    }
  }

  def getAllUsers(): Seq[InternalUser] = {
    val ps = conn.prepareStatement(rawUserSql)

    createSeq(ps.executeQuery(), extractUserFromRs)
  }

  def getRawUserFromUuid(uuid: UUID): InternalUser = {
    val sql = rawUserSql + "\n and u.id = cast(? as uuid)"

    val ps = conn.prepareStatement(sql)
    ps.setString(1, uuid)

    extractSingleUserFromRs(ps.executeQuery())
  }

  def getRawUserFromEmail(email: String): InternalUser = {
    val sql = rawUserSql + "\n and ua.email = ?"

    val ps = conn.prepareStatement(sql)
    ps.setString(1, email)

    extractSingleUserFromRs(ps.executeQuery())
  }

  def getRawUserFromEmailOptional(email: String): Option[InternalUser] = {
    Try(getRawUserFromEmail(email)).toOption
  }

  def updateUser(user: UserParameters)(implicit iu: InternalUser): UUID = {
    val ur = getRawUserFromEmail(user.loginParameters.email)
    updateUserRecords(ur.copy(
      email = user.loginParameters.email,
      hash = hashPassword(user.loginParameters.password, ur.salt),
      name = user.name
    ), true)
  }

  def deleteUser(user: UserParameters)(implicit iu: InternalUser): UUID = {
    val ur = getRawUserFromEmail(user.loginParameters.email)
    updateUserRecords(ur, false)
  }

  def insertUser(user: UserParameters)(implicit au: InternalUser): UUID = {
    if (getRawUserFromEmailOptional(user.loginParameters.email).isEmpty) {
      throw new EmailAlreadyExistsException(user.loginParameters.email)
    }
    val baseMetaId = insertMetadataStatement(conn, true)
    val userId = getUUID()
    val createBaseRecordSql =
      s"""
         |insert into user_account
         |(id, metadata_id)
         |values (cast(? as uuid), ?)
       """.stripMargin
    val ps = conn.prepareStatement(createBaseRecordSql)
    ps.setString(1, userId)
    ps.setInt(2, baseMetaId)
    ps.executeUpdate()

    val salt = generateSalt()
    val hash = hashPassword(user.loginParameters.password, salt)
    val iu = InternalUser(
      userId,
      user.loginParameters.email,
      user.name,
      salt,
      hash,
      false,
      Some(getUUID()),
      false,
      false
    )
    updateUserRecords(iu, true)
  }

  private def updateUserRecords(t: InternalUser, isValid: Boolean)(implicit iu: InternalUser): UUID = {
    insertUserAccessData(t, isValid)
    insertUserAttribute(t, isValid)
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
      Some(resultSet.getString("reset_token")),
      resultSet.getBoolean("user_lock"),
      resultSet.getBoolean("admin_lock")
    )
  }

  private def insertUserAccessData(t: InternalUser, isValid: Boolean)(implicit iu: InternalUser): Unit = {
    val mId = insertMetadataStatement(conn, isValid)
    val createAccessSql =
      s"""
         |with new_record (user_id,
         |       salt,
         |       hash,
         |       confirmed,
         |       admin_lock,
         |       user_lock,
         |       reset_token,
         |       metadata_id) as (values (cast(? as uuid), ?, ?, ?, ?, ?, cast(? as uuid), ?))
         |insert into user_access (user_id, salt, hash, confirmed, admin_lock, user_lock, reset_token, metadata_id)
         |select nr.user_id, nr.salt, nr.hash, nr.confirmed, nr.admin_lock, nr.user_lock, nr.reset_token, nr.metadata_id
         |from new_record nr
         |       left join (select distinct on (ua.user_id) ua.user_id,
         |                                     ua.salt,
         |                                     ua.hash,
         |                                     ua.confirmed,
         |                                     ua.admin_lock,
         |                                     ua.user_lock,
         |                                     ua.reset_token,
         |                                     mua.is_valid
         |                  from user_access ua
         |                  left join metadata mua on ua.metadata_id = mua.rid
         |                  order by ua.user_id, ua.rid desc) er on nr.user_id = er.user_id and is_valid
         |where not (nr.salt = er.salt and nr.hash = er.hash and nr.confirmed = er.confirmed and
         |           nr.admin_lock = er.admin_lock and
         |           nr.user_lock = er.user_lock and nr.reset_token = er.reset_token
         |           and mua.is_valid = $isValid
         |           ) or (er.user_id is null);
       """.stripMargin
    val psAccess = conn.prepareStatement(createAccessSql)
    psAccess.setString(1, t.id)
    psAccess.setString(2, t.salt)
    psAccess.setString(3, t.hash)
    psAccess.setBoolean(4, t.confirmed)
    psAccess.setBoolean(5, t.adminLock)
    psAccess.setBoolean(6, t.userLock)
    psAccess.setNullableString(7, t.resetToken.map(_.toString))
    psAccess.setInt(8, mId)
    psAccess.executeUpdate()
  }

  private def insertUserAttribute(t: InternalUser, isValid: Boolean)(implicit iu: InternalUser): Unit = {
    val m2id = insertMetadataStatement(conn, true)
    val psAttributeSql =
      s"""
        |with new_record (user_id,
        |       email,
        |       name,
        |       metadata_id) as (values (cast(? as uuid), ?, ?, ?))
        |insert into user_attribute (user_id, email, name, metadata_id)
        |select nr.user_id, nr.email, nr.name, nr.metadata_id
        |from new_record nr
        |       left join (select distinct on (ua.user_id) ua.user_id, ua.email, ua.name, mua.is_valid
        |                  from user_attribute ua
        |                         left join metadata mua on ua.metadata_id = mua.rid
        |                  order by ua.user_id, ua.rid desc) er on nr.user_id = er.user_id and is_valid
        |where not (nr.email = er.email and nr.name = er.name and mua.is_valid = $isValid)
        |   or (er.user_id is null);
      """.stripMargin
    val psAttribute = conn.prepareStatement(psAttributeSql)
    psAttribute.setString(1, t.id)
    psAttribute.setString(2, t.email)
    psAttribute.setString(3, t.name)
    psAttribute.setInt(4, m2id)
    psAttribute.executeUpdate()
  }


  private lazy val rawUserSql =
    s"""
       |select distinct on (id) u.id,
       |       ua.name,
       |       ua.email,
       |       ua.name,
       |       access.hash,
       |       access.salt,
       |       access.confirmed,
       |       access.reset_token,
       |       access.user_lock,
       |       access.admin_lock,
       |       uam.is_valid as uam_valid
       |  from user_account u
       |left join user_attribute ua on u.id = ua.user_id
       |left join metadata uam on ua.metadata_id = uam.rid
       |left join (
       |      select distinct on (user_id)
       |       access.user_id,
       |       access.hash,
       |       access.salt,
       |       access.confirmed,
       |       access.reset_token,
       |       access.user_lock,
       |       access.admin_lock,
       |             m2.is_valid
       |      from user_access access
       |      inner join metadata m2 on access.metadata_id = m2.rid
       |      order by access.user_id, access.rid desc
       |      ) access
       |      on access.user_id = u.id and access.is_valid
       |where uam.is_valid""".stripMargin
}
