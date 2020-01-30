package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet, Time}
import java.time.LocalTime
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.service.{ScheduleNotFoundException, ServiceNotFoundException}
import org.downtowndailybread.bethsaida.model.{InternalUser, Schedule, ScheduleDetail, Service, ServiceAttributes, ServiceType}
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}
import org.downtowndailybread.bethsaida.providers.{SettingsProvider, UUIDProvider}

class ServiceRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {

  implicit private def timeConverter(t: Time): LocalTime = t.toLocalTime

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
         |insert into service (id, name, type, default_capacity, metadata_id)
         |VALUES (cast(? as uuid), ?, ?, ?, ?)
      """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setString(1, id)
    ps.setString(2, attributes.name)
    ps.setString(3, attributes.serviceType.toString)
    ps.setNullableInt(4, attributes.defaultCapacity)
    ps.setInt(5, metaId)
    ps.executeUpdate()
    id
  }

  def updateService(id: UUID, attribute: ServiceAttributes)(
    implicit iu: InternalUser
  ): Unit = {
    val sql =
      s"""
         |update service
         |  set name = ?,
         |      type = ?,
         |      default_capacity = ?
         |where id = cast(? as uuid)
       """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setString(1, attribute.name)
    ps.setString(2, attribute.serviceType.toString)
    ps.setNullableInt(3, attribute.defaultCapacity)
    ps.setString(4, id)
    ps.executeUpdate()
  }

  def deleteService(id: UUID)(
    implicit iu: InternalUser
  ): Unit = {
    val sql =
      s"""
         |delete from service
         |where id = cast(? as uuid)
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, id)
    ps.executeUpdate()
  }

  private def serviceGetter(uuid: Option[UUID]): Seq[Service] = {
    val (serviceFilter, schedFilter, id: String) = uuid match {
      case Some(i) => ("a.service_id = cast(? as uuid)", "sched.service_id = cast(? as uuid)", i)
      case None => ("(1 = 1 or '' = ?)", "(1 = 1 or '' = ?)", "")
    }
    val sql =
      s"""
         |select id,
         |       name,
         |       type,
         |       default_capacity
         |from service
         |where $serviceFilter
         | and $schedFilter
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, id)
    ps.setString(2, id)
    val rs = ps.executeQuery()

    val serviceAttributes = createSeq(rs, serviceRsConverter)

    val schedules = new ScheduleRequest(settings, conn).getSchedulesByServiceId(serviceAttributes.map(_._1))

    serviceAttributes.map{
      case (id, attributes) => Service(
        id,
        attributes,
        schedules.filter(_.serviceId == id)
      )
    }
  }

  private def serviceRsConverter(rs: ResultSet): (UUID, ServiceAttributes) = {
    (rs.getString("id"),
      ServiceAttributes(
        rs.getString("name"),
        ServiceType.withName(rs.getString("type")),
        Option(rs.getInt("defaut_capacity"))
      )
    )
  }
}
