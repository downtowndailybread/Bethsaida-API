package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.client.DuplicateBanException
import org.downtowndailybread.bethsaida.model.{Ban, BanAttribute, BanType, InternalUser}
import org.downtowndailybread.bethsaida.providers.{HashProvider, UUIDProvider}
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}

class BanRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider
    with HashProvider {

  def insertBan(clientId: UUID, ban: BanAttribute)(implicit iu: InternalUser): UUID = {
    val id = UUID.randomUUID()
    val sql =
      s"""
         insert into ban
         (id, client_id, type, stop, user_id, notes, start)
         values
         (cast(? as uuid), cast(? as uuid), ?, ?, cast(? as uuid), ?, ?)
         """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setUUID(1, id)
    ps.setUUID(2, clientId)
    ps.setString(3, ban.banType.str)
    ps.setNullableTimestamp(4, ban.stopTime match {
      case Some(d) => Some(Timestamp.valueOf(ZonedDateTime.of(d, ZoneId.of("America/New_York")).toLocalDateTime))
      case _ => None
    })
    ps.setUUID(5, iu.id)
    ps.setNullableString(6, ban.notes)
    ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()))
    ps.executeUpdate()
    id
  }

  def getBanFromClientId(id: UUID): Option[Ban] = {
    val sql = s"${rawUserSql}\n and client_id = cast(? as uuid)"
    val ps = conn.prepareStatement(sql)
    ps.setUUID(1, id)
    val result = ps.executeQuery()
    val seq = createSeq(result, extractBanFromRs).filter(_.attributes.isActive(LocalDateTime.now()))
    if(seq.size > 1) {
      throw new DuplicateBanException()
    }
    seq.headOption
  }

  def deleteBan(clientId: UUID): Unit = {
    val sql =
      s"""
         delete from ban
         where client_id = cast(? as uuid)
         """
    val ps = conn.prepareStatement(sql)
    ps.setUUID(1, clientId)

    ps.executeUpdate()
  }


  private def extractBanFromRs(rs: ResultSet): Ban = {
    Ban(
      rs.getUUID("id"),
      rs.getUUID("client_id"),
      new UserRequest(settings, conn).getRawUserFromUuid(rs.getUUID("user_id")),
      BanAttribute(
        rs.getLocalDateTime("start"),
        rs.getOptionalLocalDateTime("stop"),
        BanType.apply(rs.getString("type")),
        rs.getOptionalString("notes")
      )
    )
  }


  private lazy val rawUserSql =
    s"""
       |select id, client_id, type, stop, user_id, notes, start
       |from ban
       |where 1=1
     """.stripMargin
}
