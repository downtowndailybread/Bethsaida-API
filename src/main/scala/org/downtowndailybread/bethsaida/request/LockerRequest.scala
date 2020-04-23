package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.LocalDateTime
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.model.{Locker, LockerDetails, Note}
import org.downtowndailybread.bethsaida.providers.UUIDProvider
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}

class LockerRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {


  def getLockers(): List[Locker] = {

    val sql =
      s"""
         |select l.id, l.client_id, l.locker_number, l.start_date, l.expected_end_date, l.end_date, l.input_user
         |from locker l
         |where end_date is null
         |""".stripMargin


    val ps = conn.prepareStatement(sql)

    val result = ps.executeQuery()

    val rsConverter = (rs: ResultSet) => Locker(
      result.getUUID("id"),
      LockerDetails(result.getString("locker_number"),
        result.getUUID("client_id"),
        result.getLocalDateTime("start_date"),
        None,
        result.getLocalDateTime("expected_end_date"),
        result.getUUID("input_user")
      ))

    val it = new Iterator[Locker] {
      override def hasNext: Boolean = result.next()

      override def next(): Locker = rsConverter(result)
    }

    it.toList
  }

  def removeLocker(id: UUID, time: LocalDateTime): UUID = {
    val sql =
      s"""
         |update locker
         |set end_date = ?
         |where id = cast(? as uuid)""".stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setTimestamp(1, Timestamp.valueOf(time))
    ps.setUUID(2, id)
    ps.executeUpdate()

    id
  }


  def insertLocker(lockerDetails: LockerDetails): UUID = {
    val id = UUID.randomUUID()
    val s =
      s"""
         |insert into locker (id, locker_number, start_date, expected_end_date, end_date, input_user, client_id)
         |values (cast(? as uuid), ?, ?, ?, ?, cast(? as uuid), cast(? as uuid))
         |""".stripMargin

    val ps = conn.prepareStatement(s)
    ps.setUUID(1, id)
    ps.setString(2, lockerDetails.lockerNumber)
    ps.setLocalDateTime(3, lockerDetails.startDate)
    ps.setLocalDateTime(4, lockerDetails.expectedEndDate)
    ps.setLocalDateTimeOption(5, lockerDetails.endDate)
    ps.setUUID(6, lockerDetails.inputUser)
    ps.setUUID(7, lockerDetails.clientId)

    ps.executeUpdate()
    id
  }

  def editLocker(id: UUID, lockerDetails: LockerDetails): UUID = {
    val s =
      s"""
         |update locker
         |set locker_number = ?, start_date = ?, expected_end_date = ?, end_date = ?
         |where id = cast(? as uuid)""".stripMargin

    val ps = conn.prepareStatement(s)
    ps.setString(1, lockerDetails.lockerNumber)
    ps.setLocalDateTime(2, lockerDetails.startDate)
    ps.setLocalDateTime(3, lockerDetails.expectedEndDate)
    ps.setLocalDateTimeOption(4, lockerDetails.endDate)
    ps.setUUID(5, id)

    ps.executeUpdate()
    id
  }
}
