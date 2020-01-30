package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet, Timestamp}
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.client.ClientNotFoundException
import org.downtowndailybread.bethsaida.model.{Client, InternalUser, UpsertClient}
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}
import org.downtowndailybread.bethsaida.providers.UUIDProvider


class ClientRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {

  def getClients(id: Option[UUID] = None): Seq[Client] = {
    val sql =
      s"""
         |select
         |       c.id,
         |       c.active,
         |       c.first_name,
         |       c.middle_name,
         |       c.last_name,
         |       c.date_of_birth,
         |       c.client_photo,
         |       c.race,
         |       c.gender,
         |       c.client_photo_id
         |from client c
         |where c.active = true
      """.stripMargin
    val statement = conn.prepareStatement(sql)
    val result = statement.executeQuery()

    val nicknameSql =
      s"""
         |select
         |  c.client_id,
         |  c.nickname
         |FROM client_alias c
         |""".stripMargin

    val nicknameStatement = conn.prepareStatement(nicknameSql)
    val nicknameResult = nicknameStatement.executeQuery()
    val nicknameMap = createSeq(
      nicknameResult,
      createNicknameFromResultSet
    ).groupBy(_._1).map { case (k, v) => (k, v.map(_._2)) }

    createSeq(
      result,
      createClientFromResultSet(nicknameMap)
    )
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
      upsertClient.nicknames.get,
      upsertClient.dateOfBirth.get,
      upsertClient.photoIdTag.get
    )
    val sql =
      s"""
         |insert into client
         | (id, active, first_name, last_name, date_of_birth, photo_id_tag, middle_name)
         |VALUES (cast(? as uuid), true, ?, ?, ?, ?, ?)
         |""".stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, client.id)
    ps.setString(2, client.firstName)
    ps.setString(3, client.lastName)
    ps.setTimestamp(4, Timestamp.valueOf(client.dateOfBirth.atStartOfDay()))
    ps.setString(5, client.photoIdTag)
    ps.setNullableString(6, client.middleName)
    ps.executeUpdate()

    val nicknameSql =
      s"""
         |insert into client_alias
         |(client_id, nickname)
         |values (cast(? as uuid), ?)
         |""".stripMargin

    val nps = conn.prepareStatement(nicknameSql)
    client.nicknames.foreach {
      nickname =>
        nps.setString(1, id)
        nps.setString(2, nickname)
        nps.addBatch()
    }

    nps.executeBatch()

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
                    upsertClient: UpsertClient
                  )(implicit au: InternalUser): Unit = {
    val client = {
      val oldClient = getClientById(id)
      oldClient.copy(
        firstName = upsertClient.firstName.getOrElse(oldClient.firstName),
        lastName = upsertClient.lastName.getOrElse(oldClient.lastName),
        nicknames = upsertClient.nicknames.getOrElse(oldClient.nicknames),
        dateOfBirth = upsertClient.dateOfBirth.getOrElse(oldClient.dateOfBirth),
        photoIdTag = upsertClient.photoIdTag.getOrElse(oldClient.photoIdTag)
      )
    }

    val sql =
      s"""
         |update client
         |set first_name = ?,
         |last_name = ?,
         |date_of_birth = ?,
         |photo_id_tag = ?
         |where id = (cast(? as uuid))
         |""".stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setString(1, client.firstName)
    ps.setString(2, client.lastName)
    ps.setTimestamp(3, Timestamp.valueOf(client.dateOfBirth.atStartOfDay()))
    ps.setString(4, client.photoIdTag)
    ps.setString(5, id)
    ps.executeUpdate()

    val allNicknamesSql =
      s"""
         |select client_id, nickname
         |from client_alias
         |where client_id = (cast(? as uuid))
         |""".stripMargin

    val nps = conn.prepareStatement(allNicknamesSql)
    nps.setString(1, client.id)
    val nrs = nps.executeQuery()
    val allNicknames = createSeq(nrs, createNicknameFromResultSet).map(_._2)

    val toAdd = client.nicknames.diff(allNicknames)
    val toDelete = allNicknames.diff(client.nicknames)

    if (toDelete.nonEmpty) {
      val deleteNicknames =
        s"""
           |delete from client_alias
           |where client_id = (cast(? as uuid))
           |and nickname = ?
           |""".stripMargin
      val dps = conn.prepareStatement(deleteNicknames)

      toDelete.foreach {
        nickname =>
          dps.setString(1, client.id)
          dps.setString(2, nickname)
          dps.addBatch()
      }
      dps.executeBatch()
    }


    if (toAdd.nonEmpty) {
      val addNicknames =
        s"""
           |insert into client_alias (client_id, nickname)
           |values (cast(? as uuid), ?)
           |""".stripMargin
      val aps = conn.prepareStatement(addNicknames)

      toAdd.foreach {
        nickname =>
          aps.setString(1, client.id)
          aps.setString(2, nickname)
          aps.addBatch()
      }
      aps.executeBatch()
    }
  }

  private def createClientFromResultSet(nicknameMap: Map[UUID, Seq[String]])(rs: ResultSet): Client = {
    val id = rs.getUUID("id")
    Client(
      id,
      rs.getString("first_name"),
      rs.getOptionalString("middle_name"),
      rs.getString("last_name"),
      nicknameMap.getOrElse(id, Seq()),
      rs.getLocalDate("date_of_birth"),
      rs.getString("photo_id_tag")
    )
  }

  private def createNicknameFromResultSet(rs: ResultSet): (UUID, String) = {
    (rs.getUUID("client_id"), rs.getString("nickname"))
  }
}
