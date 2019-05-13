package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet}
import java.time.LocalTime
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.model.{EventAttendance, EventAttendanceAttribute, InternalUser}
import org.downtowndailybread.bethsaida.providers.{SettingsProvider, UUIDProvider}
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}

class AttendanceRequest(val conn: Connection, val settings: Settings)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider
    with SettingsProvider {

  def getEventAttendanceById(attendanceId: UUID): EventAttendance = {
    val sql = getEventAttendanceSql("a.id = ?")
    val ps = conn.prepareStatement(sql)
    ps.setString(1, attendanceId)

    getSingle(ps.executeQuery(), createRs)
  }

  def getEventAttendanceByClientId(clientId: UUID): Seq[EventAttendance] = {
    val sql = getEventAttendanceSql("a.client_id = ?")
    val ps = conn.prepareStatement(sql)
    ps.setString(1, clientId)

    createSeq(ps.executeQuery(), createRs)
  }

  def getEventAttendanceByEventId(eventId: UUID): Seq[EventAttendance] = {
    val sql = getEventAttendanceSql("a.event_id = ?")
    val ps = conn.prepareStatement(sql)
    ps.setString(1, eventId)

    createSeq(ps.executeQuery(), createRs)
  }

  def updateEventAttendance(attendanceId: UUID, attrib: EventAttendanceAttribute)(
    implicit user: InternalUser
  ): Unit = {
    insertEventAttendanceAttributeInternal(attendanceId, attrib, true)
  }

  def deleteEventAttendance(attendanceId: UUID)(
    implicit user: InternalUser
  ): Unit = {
    val attrib = getEventAttendanceById(attendanceId).attribute
    insertEventAttendanceAttributeInternal(attendanceId, attrib, false)
  }

  def createEventAttendance(eventId: UUID, clientId: UUID, attrib: EventAttendanceAttribute)(
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

    insertEventAttendanceAttributeInternal(attendanceId, attrib, true)

    attendanceId
  }

  private def createRs(rs: ResultSet): EventAttendance = {
    EventAttendance(
      rs.getString("id"),
      EventAttendanceAttribute(
        rs.getZoneDateTime("check_in_time")
      )
    )
  }

  private def insertEventAttendanceAttributeInternal(
                                                      id: UUID,
                                                      attrib: EventAttendanceAttribute,
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

  private def getEventAttendanceSql(filter: String): String = {
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
