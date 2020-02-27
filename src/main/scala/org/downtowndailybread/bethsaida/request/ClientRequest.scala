package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet, Timestamp}
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.client.ClientNotFoundException
import org.downtowndailybread.bethsaida.model.{Client, Gender, InternalUser, Race, UpsertClient}
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}
import org.downtowndailybread.bethsaida.providers.UUIDProvider


class ClientRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {

  def getClients(id: Option[UUID] = None): Seq[Client] = {

    val predicate = id match {
      case Some(id) => s"id = cast('${id.toString}' as uuid)"
      case None => s"1=1"
    }
    val sql =
      s"""
         |select
         |       c.id,
         |       c.active,
         |       c.first_name,
         |       c.last_name,
         |       c.date_of_birth,
         |       c.client_photo,
         |       c.middle_name,
         |       c.race,
         |       c.phone,
         |       c.gender,
         |       c.client_photo_id,
         |       c.intake_date
         |from client c
         |where c.active = true
         |AND $predicate
      """.stripMargin
    val statement = conn.prepareStatement(sql)
    val result = statement.executeQuery()

//    val nicknameSql =
//      s"""
//         |select
//         |  c.client_id,
//         |  c.nickname
//         |FROM client_alias c
//         |""".stripMargin
//
//    val nicknameStatement = conn.prepareStatement(nicknameSql)
//    val nicknameResult = nicknameStatement.executeQuery()
//    val nicknameMap = createSeq(
//      nicknameResult,
//      createNicknameFromResultSet
//    ).groupBy(_._1).map { case (k, v) => (k, v.map(_._2)) }
//
    val r = createSeq(
      result,
      createClientFromResultSet()
    )

    r.toList
  }

  private def getClientOptionById(id: UUID): Option[Client] = {
    getClients(Some(id)).headOption
  }

  def getClientById(id: UUID): Client = {
    getClientOptionById(id) match {
      case Some(client) => client
      case None => throw new ClientNotFoundException(id)
    }
  }


  def insertClient(upsertClient: UpsertClient)(implicit au: InternalUser): UUID = {
    val id = getUUID()
    val client = Client(
      id,
      upsertClient.firstName.get,
      upsertClient.middleName,
      upsertClient.lastName.get,
      upsertClient.dateOfBirth.get,
      upsertClient.gender.get,
      upsertClient.race.get,
      upsertClient.phone,
      upsertClient.clientPhoto,
      upsertClient.photoId,
      upsertClient.intakeDate,
      Some(au)
    )
    val sql =
      s"""
         |insert into client
         | (id, active, first_name, last_name, date_of_birth, client_photo, middle_name, race, phone, gender, client_photo_id, intake_date, intake_user_id)
         |VALUES (cast(? as uuid), true, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, cast(? as uuid))
         |""".stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, client.id)
    ps.setString(2, client.firstName)
    ps.setString(3, client.lastName)
    ps.setTimestamp(4, Timestamp.valueOf(client.dateOfBirth.atStartOfDay()))
    ps.setNullableString(5, client.clientPhoto)
    ps.setNullableString(6, client.middleName)
    ps.setString(7, client.race.string)
    ps.setNullableInt(8, client.phone)
    ps.setString(9, client.gender.string)
    ps.setNullableString(10, client.photoId)
    ps.setNullableTimestamp(11, client.intakeDate.map(ts => Timestamp.valueOf(ts.atStartOfDay())))
    ps.setString(12, au.id)
    ps.executeUpdate()

//    val nicknameSql =
//      s"""
//         |insert into client_alias
//         |(client_id, nickname)
//         |values (cast(? as uuid), ?)
//         |""".stripMargin
//
//    val nps = conn.prepareStatement(nicknameSql)
//    client.nicknames.foreach {
//      nickname =>
//        nps.setString(1, id)
//        nps.setString(2, nickname)
//        nps.addBatch()
//    }
//
//    nps.executeBatch()

    id
  }


  def deleteClient(id: UUID): Unit = {
    val sql =
      s"""
        update client
        set active = false
        where id = cast(? as uuid)
        ;
        """
    val ps = conn.prepareStatement(sql)
    ps.setString(1, id)
    ps.executeUpdate()
  }

  def updateClient(
                    id: UUID,
                    client: UpsertClient
                  )(implicit au: InternalUser): Unit = {

    val sql =
      s"""
         |update client
         |first_name = ?,
         |last_name = ?,
         |date_of_birth = ?,
         |client_photo = ?,
         |middle_name = ?,
         |race = ?,
         |phone = ?,
         |gender = ?,
         |client_photo_id = ?
         |intake_date = ?
         |where id = (cast(? as uuid))
         |""".stripMargin
    val ps = conn.prepareStatement(sql)

    ps.setString(1, client.firstName.get)
    ps.setString(2, client.lastName.get)
    ps.setTimestamp(3, Timestamp.valueOf(client.dateOfBirth.get.atStartOfDay()))
    ps.setNullableString(4, client.clientPhoto)
    ps.setNullableString(5, client.middleName)
    ps.setString(6, client.race.get.string)
    ps.setNullableInt(7, client.phone)
    ps.setString(8, client.gender.get.string)
    ps.setNullableString(9, client.photoId)
    ps.setTimestamp(10, Timestamp.valueOf(client.intakeDate.get.atStartOfDay()))
    ps.setString(11, id.toString)
    ps.executeUpdate()

//    val allNicknamesSql =
//      s"""
//         |select client_id, nickname
//         |from client_alias
//         |where client_id = (cast(? as uuid))
//         |""".stripMargin
//
//    val nps = conn.prepareStatement(allNicknamesSql)
//    nps.setString(1, client.id)
//    val nrs = nps.executeQuery()
//    val allNicknames = createSeq(nrs, createNicknameFromResultSet).map(_._2)
//
//    val toAdd = client.nicknames.diff(allNicknames)
//    val toDelete = allNicknames.diff(client.nicknames)
//
//    if (toDelete.nonEmpty) {
//      val deleteNicknames =
//        s"""
//           |delete from client_alias
//           |where client_id = (cast(? as uuid))
//           |and nickname = ?
//           |""".stripMargin
//      val dps = conn.prepareStatement(deleteNicknames)
//
//      toDelete.foreach {
//        nickname =>
//          dps.setString(1, client.id)
//          dps.setString(2, nickname)
//          dps.addBatch()
//      }
//      dps.executeBatch()
//    }

//
//    if (toAdd.nonEmpty) {
//      val addNicknames =
//        s"""
//           |insert into client_alias (client_id, nickname)
//           |values (cast(? as uuid), ?)
//           |""".stripMargin
//      val aps = conn.prepareStatement(addNicknames)
//
//      toAdd.foreach {
//        nickname =>
//          aps.setString(1, client.id)
//          aps.setString(2, nickname)
//          aps.addBatch()
//      }
//      aps.executeBatch()
//    }
  }

  private def createClientFromResultSet()(rs: ResultSet): Client = {
    val id = rs.getUUID("id")
    Client(
      id,
      rs.getString("first_name"),
      rs.getOptionalString("middle_name"),
      rs.getString("last_name"),
      rs.getLocalDate("date_of_birth"),
      Gender(rs.getString("gender")),
      Race(rs.getString("race")),
      rs.getOptionalInt("phone"),
      rs.getOptionalString("client_photo"),
      rs.getOptionalString("client_photo_id"),
      rs.getOptionalLocalDate("intake_date"),
      Some(new UserRequest(settings, conn).getRawUserFromUuid(rs.getUUID("intake_user")))
    )
  }

  private def createNicknameFromResultSet(rs: ResultSet): (UUID, String) = {
    (rs.getUUID("client_id"), rs.getString("nickname"))
  }
}
