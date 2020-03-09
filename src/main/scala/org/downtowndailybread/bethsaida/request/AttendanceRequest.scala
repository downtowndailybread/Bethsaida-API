package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.{LocalTime, ZoneId, ZonedDateTime}
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.model.{Attendance, AttendanceAttribute, InternalUser}
import org.downtowndailybread.bethsaida.providers.{SettingsProvider, UUIDProvider}
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}

class AttendanceRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {

  def getAttendanceById(attendanceId: UUID): Attendance = {
    val sql = getAttendanceSql("id = ?")
    val ps = conn.prepareStatement(sql)
    ps.setString(1, attendanceId)

    getSingle(ps.executeQuery(), createRs)
  }

  def getAttendanceByClientId(clientId: UUID): Seq[AttendanceAttribute] = {
    val sql = getAttendanceSql("client_id = ?")
    val ps = conn.prepareStatement(sql)
    ps.setString(1, clientId)

    createSeq(ps.executeQuery(), createRs).map(_.attribute)
  }

  def getAttendanceByEventId(eventId: UUID): Seq[Attendance] = {
    val sql = getAttendanceSql("event_id = cast(? as uuid)")
    val ps = conn.prepareStatement(sql)
    ps.setString(1, eventId)

    createSeq(ps.executeQuery(), createRs)
  }

  def updateAttendance(attendanceId: UUID, attribute: AttendanceAttribute)(
    implicit user: InternalUser
  ): Unit = {
    val sql =
      s"""
         |update attendance
         |set check_in_time  = ?,
         |    check_out_time = ?,
         |    event_id       = cast(? as uuid),
         |    client_id      = cast(? as uuid)
         |from attendance
         |where id = cast(? as uuid)
       """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setZonedDateTime(1, attribute.checkInTime)
    ps.setZonedDateTime(2, attribute.checkInTime)
    ps.setString(3, attribute.eventId)
    ps.setString(4, attribute.clientId)
    ps.setString(5, attendanceId)
    ps.executeUpdate()
  }

  def deleteAttendance(attendanceId: UUID)(
    implicit user: InternalUser
  ): Unit = {
    val sql =
      s"""
         |delete from attendance
         |where id = cast(? as uuid)
       """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setString(1, attendanceId)
    ps.executeUpdate()
  }

  def createAttendance(attrib: AttendanceAttribute)(
    implicit user: InternalUser
  ): UUID = {
    val attendanceId = getUUID()
    val sql =
      s"""
         |insert into attendance
         |    (id, check_in_time, event_id, client_id)
         |VALUES (cast(? as uuid), ?, cast(? as uuid), cast(? as uuid))
       """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setString(1, attendanceId)
    ps.setTimestamp(2, Timestamp.valueOf(attrib.checkInTime.toLocalDateTime))
//    ps.setZonedDateTime(2, attrib.checkInTime)
//    ps.setZonedDateTime(3, attrib.checkOutTime)
    ps.setString(3, attrib.eventId)
    ps.setString(4, attrib.clientId)
    ps.executeUpdate()


    attendanceId
  }

  private def createRs(rs: ResultSet): Attendance = {
    Attendance(
      rs.getString("id"),
      AttendanceAttribute(
        rs.getUUID("event_id"),
        rs.getUUID("client_id"),
        ZonedDateTime.of(rs.getTimestamp("check_in_time").toLocalDateTime, ZoneId.of("America/New_York"))
      )
    )
  }

  private def getAttendanceSql(filter: String): String = {
    s"""
       |select id,
       |       check_in_time,
       |       event_id,
       |       client_id
       |from attendance
       |where $filter
       """.stripMargin
  }
}
