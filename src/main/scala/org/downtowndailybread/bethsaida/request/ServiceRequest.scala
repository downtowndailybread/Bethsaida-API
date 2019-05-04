package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet}
import java.util.UUID

import org.downtowndailybread.bethsaida.exception.service.{ScheduleNotFoundException, ServiceNotFoundException}
import org.downtowndailybread.bethsaida.model.{InternalUser, Schedule, ScheduleDetail, Service, ServiceAttributes, ServiceType}
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}
import org.downtowndailybread.bethsaida.service.UUIDProvider

class ServiceRequest(val conn: Connection) extends BaseRequest with DatabaseRequest with UUIDProvider {

  def rsConverter(rs: ResultSet): Service = {
    Service(
      parseUUID(rs.getString("id")),
      ServiceAttributes(
        rs.getString("name"),
        ServiceType.withName(rs.getString("type"))
      ),
      if(rs.getString("schedule_id") == null) {
        Seq()
      } else {
        Seq(Schedule(
          parseUUID(rs.getString("schedule_id")),
          ScheduleDetail(
            rs.getString("rrule"),
            rs.getBoolean("enabled")
          )
        ))
      }
    )
  }

  def getAllServices(): Seq[Service] = serviceGetter(None)

  def getService(id: UUID): Service = serviceGetter(Some(id)).toList match {
    case service :: Nil => service
    case _ => throw new ServiceNotFoundException(id)
  }

  def insertService(attributes: ServiceAttributes)(implicit iu: InternalUser): UUID = {
    val metaId = insertMetadataStatement(conn, true)
    val id = getUUID()
    val sql =
      s"""
         |insert into service
         |    (id, metadata_id)
         |VALUES (cast(? as uuid), ?)
      """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setString(1, id)
    ps.setInt(2, metaId)
    ps.executeUpdate()
    updateService(id, attributes)
    id
  }

  def updateService(uuid: UUID, attribute: ServiceAttributes)(
    implicit iu: InternalUser
  ): Unit = {
    serviceSetter(uuid, attribute, true)
  }

  def deleteService(uuid: UUID)(
    implicit iu: InternalUser
  ): Unit = {
    serviceSetter(uuid, getService(uuid).attributes, false)
  }

  def insertSchedule(serviceId: UUID, schedule: ScheduleDetail)(implicit iu: InternalUser): UUID = {
    val metaId = insertMetadataStatement(conn, true)
    val id = getUUID()
    val sql =
      s"""
         |insert into service_schedule
         |    (id, service_id, metadata_id)
         |VALUES (cast(? as uuid), cast(? as uuid), ?)
       """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setString(1, id)
    ps.setString(2, serviceId)
    ps.setInt(3, metaId)
    ps.executeUpdate()
    updateSchedule(id, schedule)
    id
  }

  def updateSchedule(scheduleId: UUID, schedule: ScheduleDetail)(implicit iu: InternalUser): Unit = {
    scheduleSetter(scheduleId, schedule, true)
  }

  def deleteSchedule(scheduleId: UUID)(implicit iu: InternalUser): Unit = {
    scheduleSetter(scheduleId, getScheduleDetail(scheduleId), false)
  }

  private def serviceSetter(id: UUID, attribute: ServiceAttributes, enabled: Boolean)(
    implicit iu: InternalUser
  ): Unit = {
    val metaId = insertMetadataStatement(conn, enabled)
    val sql =
      s"""
         |insert into service_attribute
         |    (service_id, name, type, metadata_id)
         |values (cast(? as uuid), ?, ?, ?)
       """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setString(1, id)
    ps.setString(2, attribute.name)
    ps.setString(3, attribute.serviceType.toString)
    ps.setInt(4, metaId)
    ps.executeUpdate()
  }

  private def scheduleSetter(scheduleId: UUID, detail: ScheduleDetail, enabled: Boolean)(
    implicit iu: InternalUser
  ): Unit = {
    val metaId = insertMetadataStatement(conn, enabled)
    val sql =
      s"""
         |insert into service_schedule_details
         |    (schedule_id, rrule, enabled, metadata_id)
         |values (cast(? as uuid), ?, ?, ?)
       """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setString(1, scheduleId)
    ps.setString(2, detail.rrule)
    ps.setBoolean(3, detail.enabled)
    ps.setInt(4, metaId)
    ps.executeUpdate()
  }

  private def getScheduleDetail(scheduleId: UUID): ScheduleDetail = {
    val sql =
      s"""
        |select distinct on (details.schedule_id) sched.service_id,
        |                                             details.schedule_id,
        |                                             details.rrule,
        |                                             details.enabled,
        |                                             m.is_valid
        |    from service_schedule sched
        |             left join service_schedule_details details
        |                       on sched.id = details.schedule_id
        |             left join metadata m on details.metadata_id = m.rid
        |    where sched.id = cast(? as uuid)
        |    order by details.schedule_id, details.rid desc
      """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setString(1, scheduleId)
    val rs = ps.executeQuery()
    createSeq(
      rs,
      (rs) => ScheduleDetail(rs.getString("rrule"), rs.getBoolean("enabled"))
    ) match {
      case h :: Nil => h
      case _ => throw new ScheduleNotFoundException(scheduleId)
    }
  }

  private def serviceGetter(idInput: Option[UUID]): Seq[Service] = {
    val (serviceFilter, schedFilter, id) = idInput match {
      case Some(i) => ("a.service_id = cast(? as uuid)", "sched.service_id = cast(? as uuid)", i.toString)
      case None => ("(1 = 1 or '' = ?)", "(1 = 1 or '' = ?)", "")
    }
    val sql =
      s"""
         |select service.id,
         |       service.name,
         |       service.type,
         |       sched.schedule_id,
         |       sched.rrule,
         |       sched.enabled
         |from (select distinct on (a.service_id) a.service_id as id,
         |                                        a.name,
         |                                        a.type,
         |                                        m.is_valid
         |      from service_attribute a
         |               left join metadata m on a.metadata_id = m.rid
         |      where $serviceFilter
         |      order by a.service_id, a.rid desc) service
         |         left join (
         |    select distinct on (details.schedule_id) sched.service_id,
         |                                             details.schedule_id,
         |                                             details.rrule,
         |                                             details.enabled,
         |                                             m.is_valid
         |    from service_schedule sched
         |             left join service_schedule_details details
         |                       on sched.id = details.schedule_id
         |             left join metadata m on details.metadata_id = m.rid
         |    where $schedFilter
         |    order by details.schedule_id, details.rid desc
         |) sched
         |                   on service.id = sched.service_id
         |where service.is_valid
         |  and coalesce(sched.is_valid, true)
         |
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, id)
    ps.setString(2, id)
    val rs = ps.executeQuery()

    createSeq(rs, rsConverter).groupBy(s => s.id).map {
      case (id, service) => Service(id, service.head.attributes, service.flatMap(_.schedules))
    }.toSeq
  }
}
