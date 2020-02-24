package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet, Time}
import java.time.LocalTime
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.service.ScheduleNotFoundException
import org.downtowndailybread.bethsaida.model._
import org.downtowndailybread.bethsaida.providers.UUIDProvider
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}
import spray.json._

class ScheduleRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {

  implicit private def timeConverter(t: Time): LocalTime = t.toLocalTime

  def insertSchedule(serviceId: UUID, schedule: ScheduleDetail)(implicit iu: InternalUser): UUID = {
    val id = getUUID()
    val sql =
      s"""
         |insert into schedule
         |    (id, service_id, rrule, start_time, service_parameter)
         |VALUES (cast(? as uuid), cast(? as uuid), ?, ?, ?)
       """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setString(1, id)
    ps.setString(2, serviceId)
    ps.setString(3, schedule.rrule)
    ps.setTime(4, java.sql.Time.valueOf(schedule.startTime))
    ps.setString(5, "".parseJson.toString)
    ps.executeUpdate()
    id
  }

  def updateSchedule(scheduleId: UUID, schedule: ScheduleDetail)(implicit iu: InternalUser): Unit = {
    val sql =
      s"""
         |update schedule
         |set rrule = ?,
         |    start_time = ?,
         |    service_parameter = ?
         |where id = cast(? as uuid)
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, schedule.rrule)
    ps.setTime(2, java.sql.Time.valueOf(schedule.startTime))
    ps.setString(3, "")
    ps.setString(4, scheduleId)

    ps.executeUpdate()
  }

  def deleteSchedule(scheduleId: UUID)(implicit iu: InternalUser): Unit = {
    val sql =
      s"""
         |delete from schedule
         |where id = cast(? as uuid)
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, scheduleId)
    ps.executeUpdate()
  }

  def getSchedulesByServiceId(serviceIds: Seq[UUID]): Seq[Schedule] = {
    val filter = serviceIds.map(_ => "cast(? as uuid)").mkString("(", ", ", ")")
    val sql =
      s"""
         |select id,
         |       service_id,
         |       rrule,
         |       start_time,
         |       service_parameters
         |from schedule
         |where service_id in $filter
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    serviceIds.zipWithIndex.foreach{
      case (id, idx) => {
        ps.setString(idx + 1, id)
      }
    }
    val rs = ps.executeQuery()
    createSeq(rs, scheduleRsConverter)
  }

  private def scheduleRsConverter(rs: ResultSet): Schedule = {
    Schedule(
      rs.getUUID("id"),
      rs.getUUID("service_id"),
      ScheduleDetail(
        rs.getString("rrule"),
        rs.getTime("start_time"),
        rs.getTime("start_time"),
        None,
        true
      )
    )
  }

}
