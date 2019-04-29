package org.downtowndailybread.request

import java.sql.{Connection, ResultSet}
import java.util.UUID

import org.downtowndailybread.exception.user.{EmailAlreadyExistsException, UserNotFoundException}
import org.downtowndailybread.model.{InternalUser, LoginParameters, NewUserParameters}
import org.downtowndailybread.service.{HashProvider, UUIDProvider}

class UserRequest(val conn: Connection)
  extends DatabaseRequest
    with UUIDProvider
    with HashProvider {

  private val rawUserSql =
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
       |  from b_user u
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


  private def extractUserFromRs(resultSet: ResultSet): Option[InternalUser] = {
    val results = createSeq(resultSet, (rs) => {
      InternalUser(
        parseUUID(rs.getString("id")),
        rs.getString("email"),
        rs.getString("name"),
        rs.getString("salt"),
        rs.getString("hash"),
        rs.getBoolean("confirmed"),
        parseUUID(rs.getString("reset_token")),
        rs.getBoolean("user_lock"),
        rs.getBoolean("admin_lock")
      )
    })

    if (results.size > 1) {
      throw new UserNotFoundException()
    }
    results.headOption
  }

  def getRawUserFromUuid(uuid: UUID): Option[InternalUser] = {
    val sql = rawUserSql + "\n and u.id = cast(? as uuid)"

    val ps = conn.prepareStatement(sql)
    ps.setString(1, uuid.toString)

    extractUserFromRs(ps.executeQuery())
  }

  def getRawUserFromEmail(email: String): Option[InternalUser] = {
    val sql = rawUserSql + "\n and ua.email = ?"

    val ps = conn.prepareStatement(sql)
    ps.setString(1, email)

    extractUserFromRs(ps.executeQuery())
  }

  def insertLoginInfoRecord(t: InternalUser): Unit = {
    val mId = insertMetadataStatement(conn, true)
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
         |           nr.user_lock = er.user_lock and nr.reset_token = er.reset_token) or (er.user_id is null);
       """.stripMargin
    val psAccess = conn.prepareStatement(createAccessSql)
    psAccess.setString(1, t.id.toString)
    psAccess.setString(2, t.salt)
    psAccess.setString(3, t.hash)
    psAccess.setBoolean(4, t.confirmed)
    psAccess.setBoolean(5, t.adminLock)
    psAccess.setBoolean(6, t.userLock)
    psAccess.setString(7, t.resetToken.toString)
    psAccess.setInt(8, mId)
    psAccess.executeUpdate()

    val m2id = insertMetadataStatement(conn, true)
    val psAttributeSql =
      """
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
        |where not (nr.email = er.email and nr.name = er.name)
        |   or (er.user_id is null);
      """.stripMargin
    val psAttribute = conn.prepareStatement(psAttributeSql)
    psAttribute.setString(1, t.id.toString)
    psAttribute.setString(2, t.email)
    psAttribute.setString(3, t.name)
    psAttribute.setInt(4, m2id)
    psAttribute.executeUpdate()

  }

  def insertClient(user: NewUserParameters): Unit = {
    if (getRawUserFromEmail(user.loginParameters.email).nonEmpty) {
      throw new EmailAlreadyExistsException(user.loginParameters.email)
    }
    val baseMetaId = insertMetadataStatement(conn, true)
    val userId = getUUID()
    val createBaseRecordSql =
      s"""
         |insert into b_user
         |(id, metadata_id)
         |values (cast(? as uuid), ?)
       """.stripMargin
    val ps = conn.prepareStatement(createBaseRecordSql)
    ps.setString(1, userId.toString)
    ps.setInt(2, baseMetaId)
    ps.executeUpdate()

    val salt = generateSalt()
    val hash = hashPassword(user.loginParameters.password, salt)
    val iu = InternalUser(userId, user.loginParameters.email, user.name, salt, hash, false, getUUID(), false, false)
    insertLoginInfoRecord(iu)
  }

  def confirmEmail(email: String, confirmation: UUID): Boolean = {
    getRawUserFromEmail(email) match {
      case Some(user) if !user.confirmed && user.resetToken == confirmation =>
        insertLoginInfoRecord(user.copy(confirmed = true))
        true
      case _ => false
    }
  }
}
