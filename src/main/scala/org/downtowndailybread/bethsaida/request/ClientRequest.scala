package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet, Timestamp}
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.client.ClientNotFoundException
import org.downtowndailybread.bethsaida.model._
import org.downtowndailybread.bethsaida.providers.UUIDProvider
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}


class ClientRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {

  def getAllClients(id: List[UUID] = List()): Seq[Client] = {

    val predicate = id match {
      case Nil => s"1=1"
      case _ => s"c.id in (" + id.map(ids => s"cast('${ids.toString}' as uuid)").mkString("(", ", ", ")") + ")"
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
         |       c.photo_id,
         |       c.intake_date,
         |       c.intake_user,
         |       not b.id is null as is_banned,
         |       b.id as banned_id,
         |       c.secondary_race as race_secondary,
         |       c.hispanic,
         |       c.caseworker_name,
         |       c.caseworker_phone,
         |       c.last_4_ssn,
         |       c.veteran,
         |       c.covid_vaccine
         |from client c
         |left join ban b
         |on c.id = b.client_id and current_timestamp > b.start and (b.stop is null or current_timestamp < b.stop)
         |where c.active = true
         |AND $predicate
      """.stripMargin
    val statement = conn.prepareStatement(sql)
    val result = statement.executeQuery()

    val r = createSeq(
      result,
      createClientFromResultSet()
    )

    r.toList
  }

  private def getClientOptionById(id: UUID): Option[Client] = {
    getAllClients(List(id)).headOption
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
      upsertClient.intakeUserId.get,
      false,
      None,
      upsertClient.raceSecondary,
      upsertClient.hispanic.getOrElse(false),
      upsertClient.caseworkerName,
      upsertClient.caseworkerPhone,
      upsertClient.last4Ssn,
      upsertClient.veteran.getOrElse(false),
      upsertClient.covidVaccine.getOrElse(false)
    )
    val sql =
      s"""
         |insert into client
         | (id, active, first_name, last_name, date_of_birth, client_photo, middle_name, race, phone, gender, photo_id, intake_date, intake_user, secondary_race, hispanic, caseworker_name, caseworker_phone, last_4_ssn, veteran, covid_vaccine)
         |VALUES (cast(? as uuid), true, ?, ?, ?, cast(? as uuid), ?, ?, ?, ?, cast(? as uuid), ?, cast(? as uuid), ?, ?, ?, ?, ?, ?, ?)
         |""".stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, client.id)
    ps.setString(2, client.firstName)
    ps.setString(3, client.lastName)
    ps.setTimestamp(4, Timestamp.valueOf(client.dateOfBirth.atStartOfDay()))
    ps.setNullableUUID(5, client.clientPhoto)
    ps.setNullableString(6, client.middleName)
    ps.setString(7, client.race.string)
    ps.setNullableString(8, client.phone)
    ps.setString(9, client.gender.string)
    ps.setNullableUUID(10, client.photoId)
    ps.setNullableTimestamp(11, client.intakeDate.map(ts => Timestamp.valueOf(ts.atStartOfDay())))
    ps.setUUID(12, client.intakeUserId)
    ps.setString(13, client.raceSecondary.getOrElse(NotApplicable).string)
    ps.setBoolean(14, client.hispanic)
    ps.setNullableString(15, client.caseworkerName)
    ps.setNullableString(16, client.caseworkerPhone)
    ps.setNullableString(17, client.last4Ssn)
    ps.setBoolean(18, client.veteran)
    ps.setBoolean(19, client.covidVaccine)
    ps.executeUpdate()

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

    val existingClient = getClientById(id)
    val sql =
      s"""
         |update client set
         |first_name = ?,
         |last_name = ?,
         |date_of_birth = ?,
         |client_photo = cast(? as uuid),
         |middle_name = ?,
         |race = ?,
         |phone = ?,
         |gender = ?,
         |photo_id = cast(? as uuid),
         |intake_date = ?,
         |secondary_race = ?,
         |hispanic = ?,
         |intake_user = cast(? as uuid),
         |caseworker_name = ?,
         |caseworker_phone = ?,
         |last_4_ssn = ?,
         |veteran = ?,
         |covid_vaccine = ?
         |where id = (cast(? as uuid))
         |""".stripMargin
    val ps = conn.prepareStatement(sql)

    ps.setString(1, client.firstName.get)
    ps.setString(2, client.lastName.get)
    ps.setTimestamp(3, Timestamp.valueOf(client.dateOfBirth.get.atStartOfDay()))
    ps.setNullableUUID(4, client.clientPhoto)
    ps.setNullableString(5, client.middleName)
    ps.setString(6, client.race.get.string)
    ps.setNullableString(7, client.phone)
    ps.setString(8, client.gender.get.string)
    ps.setNullableUUID(9, client.photoId)
    ps.setTimestamp(10, Timestamp.valueOf(client.intakeDate.get.atStartOfDay()))
    ps.setString(11, client.raceSecondary.getOrElse(NotApplicable).string)
    ps.setBoolean(12, client.hispanic.getOrElse(false))
    ps.setUUID(13, client.intakeUserId.get)
    ps.setNullableString(14, client.caseworkerName)
    ps.setNullableString(15, client.caseworkerPhone)
    ps.setNullableString(16, client.last4Ssn)
    ps.setBoolean(17, client.veteran.getOrElse(false))
    ps.setBoolean(18, client.covidVaccine.getOrElse(false))
    ps.setString(19, id.toString)
    ps.executeUpdate()

    val imageReq = new ImageRequest(settings, conn)
    if (existingClient.photoId != client.photoId) {
      imageReq.deleteImage(existingClient.photoId)
      imageReq.createImage(existingClient.photoId)
    }
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
      rs.getOptionalString("phone"),
      rs.getOptionalUUID("client_photo"),
      rs.getOptionalUUID("photo_id"),
      rs.getOptionalLocalDate("intake_date"),
      rs.getUUID("intake_user"),
      rs.getBoolean("is_banned"),
      rs.getOptionalUUID("banned_id"),
      rs.getOptionalString("race_secondary").map(Race.apply),
      rs.getBoolean("hispanic"),
      rs.getOptionalString("caseworker_name"),
      rs.getOptionalString("caseworker_phone"),
      rs.getOptionalString("last_4_ssn"),
      rs.getBoolean("veteran"),
      rs.getBoolean("covid_vaccine")
    )
  }

  private def createNicknameFromResultSet(rs: ResultSet): (UUID, String) = {
    (rs.getUUID("client_id"), rs.getString("nickname"))
  }

  private def makeOption[T](to: Client, from: Client, c: Client => T): Option[T] = {
    Some(c(to)).orElse(Some(c(from)))
  }

  def mergeClients(merge: MergeClients)(implicit iu: InternalUser): UUID = {

    val from = getClientById(merge.from)
    val to = getClientById(merge.to)

    val clientToUpdate = UpsertClient(
      makeOption(to, from, c => c.firstName),
      to.middleName.orElse(from.middleName),
      makeOption(to, from, c => c.lastName),
      makeOption(to, from, c => c.dateOfBirth),
      makeOption(to, from, c => c.gender),
      makeOption(to, from, c => c.race),
      to.phone.orElse(from.phone),
      to.clientPhoto.orElse(from.clientPhoto),
      to.photoId.orElse(from.photoId),
      Some(List(to.intakeDate, from.intakeDate).flatten.minBy(_.toEpochDay)),
      to.raceSecondary.orElse(from.raceSecondary),
      makeOption(to, from, c => c.hispanic),
      makeOption(to, from, c => c.intakeUserId),
      to.caseworkerName.orElse(from.caseworkerName),
      to.caseworkerPhone.orElse(from.caseworkerPhone),
      to.last4Ssn.orElse(from.last4Ssn),
      makeOption(to, from, c => c.veteran),
      makeOption(to, from, c => c.covidVaccine)
    )

    updateClient(to.id, clientToUpdate)

    val attendanceReq = new AttendanceRequest(settings, conn)
    val fromClientAttendance = attendanceReq.getFullAttendanceByClientId(from.id)
    val toClientAttendance = attendanceReq.getFullAttendanceByClientId(to.id).map(r => r.attribute)

    val toEvents = toClientAttendance.map(r => r.eventId).toSet
    val eventsToInsert = fromClientAttendance.map(r => r.attribute).filter(r => !toEvents.contains(r.eventId)).map(r => r.copy(clientId = to.id))

    eventsToInsert.foreach(r => attendanceReq.createAttendance(r, true))
    fromClientAttendance.foreach(r => attendanceReq.deleteAttendance(r.id))

    val lockerReq = new LockerRequest(settings, conn)
    val allLockers = lockerReq.getLockers()
    val oldLocker = allLockers.find(_.lockerDetails.clientId == from.id)
    oldLocker match {
      case Some(locker) if !allLockers.exists(_.lockerDetails.clientId == to.id) =>
        lockerReq.insertLocker(locker.lockerDetails.copy(clientId = to.id))
      case _ =>
    }

    val mailReq = new MailRequest(settings, conn)
    val allMail = mailReq.getMail()
    val oldMail = allMail.find(_.mailDetails.clientId == from.id)
    oldMail match {
      case Some(mail) if !allMail.exists(_.mailDetails.clientId == to.id) =>
        mailReq.insertMail(mail.mailDetails.copy(clientId = to.id))
      case _ =>
    }

    deleteClient(from.id)

    to.id
  }
}
