package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet}
import java.time.LocalTime
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
    val sql = getAttendanceSql("a.id = ?")
    val ps = conn.prepareStatement(sql)
    ps.setString(1, attendanceId)

    getSingle(ps.executeQuery(), createRs)
  }

  def getAttendanceByClientId(clientId: UUID): Seq[Attendance] = {
    val sql = getAttendanceSql("a.client_id = ?")
    val ps = conn.prepareStatement(sql)
    ps.setString(1, clientId)

    createSeq(ps.executeQuery(), createRs)
  }

  def getAttendanceByEventId(eventId: UUID): Seq[Attendance] = {
    val sql = getAttendanceSql("a.event_id = ?")
    val ps = conn.prepareStatement(sql)
    ps.setString(1, eventId)

    createSeq(ps.executeQuery(), createRs)
  }

  def updateAttendance(attendanceId: UUID, attrib: AttendanceAttribute)(
    implicit user: InternalUser
  ): Unit = {
    insertAttendanceAttributeInternal(attendanceId, attrib, true)
  }

  def deleteAttendance(attendanceId: UUID)(
    implicit user: InternalUser
  ): Unit = {
    val attrib = getAttendanceById(attendanceId).attribute
    insertAttendanceAttributeInternal(attendanceId, attrib, false)
  }

  def createAttendance(eventId: UUID, clientId: UUID, attrib: AttendanceAttribute)(
    implicit user: InternalUser
  ): UUID = {
    val metaId = insertMetadataStatement(conn, true)
    val attendanceId = getUUID()
    val sql =
      s"""
         |insert into event_attendance
         |    (id, event_id, client_id, metadata_id)
         |VALUES (cast(? as uuid), cast(? as uuid), cast(? as uuid), ?)
       """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setString(1, attendanceId)
    ps.setString(2, eventId)
    ps.setString(3, clientId)
    ps.setInt(4, metaId)
    ps.executeUpdate()

    insertAttendanceAttributeInternal(attendanceId, attrib, true)

    attendanceId
  }

  private def createRs(rs: ResultSet): Attendance = {
    Attendance(
      rs.getString("id"),
      AttendanceAttribute(
        rs.getZoneDateTime("check_in_time")
      )
    )
  }

  private def insertAttendanceAttributeInternal(
                                                 id: UUID,
                                                 attrib: AttendanceAttribute,
                                                 isValid: Boolean)(
                                                      implicit iu: InternalUser
                                                    ): Unit = {
    val metaId = insertMetadataStatement(conn, isValid)
    val sql =
      s"""
         |insert into event_attendance_attribute (event_attendance_id, check_in_time, metadata_id)
         |VALUES
         |(cast(? as uuid), ?, ?)
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, id)
    ps.setZonedDateTime(2, attrib.checkInTime)
    ps.setInt(3, metaId)
  }

  private def getAttendanceSql(filter: String): String = {
    s"""
       |select *
       |from (
       |         select distinct on (a.id) a.id,
       |                                   a.event_id,
       |                                   a.client_id,
       |                                   ea.check_in_time,
       |                                   ea_meta.is_valid
       |         from event_attendance a
       |                  left join event_attendance_attribute ea on a.id = ea.event_attendance_id
       |                  left join event e on e.id = a.event_id
       |                  left join service c on c.id = e.service_id
       |                  left join metadata ea_meta on ea.metadata_id = ea_meta.rid
       |         where 1 = 1
       |         and $filter
       |         order by a.id, ea.rid desc, e.rid desc, c.rid desc
       |     ) attendance
       |where attendance.is_valid
       """.stripMargin
  }
}
