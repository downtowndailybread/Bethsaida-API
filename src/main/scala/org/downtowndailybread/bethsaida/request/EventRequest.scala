package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.event.EventNotFoundException
import org.downtowndailybread.bethsaida.model.{Event, EventAttribute, InternalUser, ServiceType}
import org.downtowndailybread.bethsaida.providers.UUIDProvider
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}

class EventRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {

  def getAllServiceEvents(serviceId: UUID): Seq[Event] = {
    getAllEventsInternal(Some(serviceId), None, None, None)
  }

  def getEvent(eventId: UUID): Event = {
    getAllEventsInternal(None, Some(eventId), None, None) match {
      case e :: Nil => e
      case _ => throw new EventNotFoundException
    }
  }

  def createEvent(event: EventAttribute)(implicit iu: InternalUser): UUID = {
    val eventId = getUUID()
    val sql =
      s"""
         |insert into event (id, capacity, service_id, user_creator, date)
         |values (cast(? as uuid), ?, cast(? as uuid), cast(? as uuid), ?)
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, eventId)
    ps.setInt(2, event.capacity)
    ps.setString(3, event.serviceId)
    ps.setNullableUUID(4, Some(iu.id))
    ps.setTimestamp(5, Timestamp.valueOf(event.date.atStartOfDay()))
    ps.executeUpdate()
    eventId
  }

  def updateEvent(eventId: UUID, event: EventAttribute)(implicit iu: InternalUser): UUID = {
    val sql =
      s"""
         |update event
         |    set capacity = ?,
         |        service_id = cast(? as uuid),
         |        date = ?
         |where id = cast(? as uuid)
       """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setInt(1, event.capacity)
    ps.setString(2, event.serviceId)
    ps.setTimestamp(3, Timestamp.valueOf(event.date.atStartOfDay()))
    ps.setString(4, eventId.toString)
    ps.executeUpdate()
    eventId
  }

  def deleteEvent(serviceId: UUID, eventId: UUID)(implicit iu: InternalUser): Unit = {
    val sql =
      s"""
         |delete from event
         |where id = cast(? as uuid)
         |and service_id = cast(? as uuid)
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, eventId)
    ps.executeUpdate()
  }

  def getAllEvents(service: ServiceType): Seq[Event] = {
    getAllEventsInternal(None, None, None, Some(service))
  }

  def getAllActiveEvents(service: ServiceType): Seq[Event] = {
    getAllEventsInternal(None, None, Some(LocalDate.now().minusDays(7L).atStartOfDay()), Some(service))
  }

  private def getAllEventsInternal(
                                    serviceId: Option[UUID],
                                    eventId: Option[UUID],
                                    date: Option[LocalDateTime],
                                    serviceType: Option[ServiceType]
                                  ): Seq[Event] = {
    val serviceIdFilter = serviceId match {
      case Some(i) => "e.service_id = cast(? as uuid)"
      case None => "(1 = 1 or '' = ?)"
    }
    val eventIdFilter = eventId match {
      case Some(i) => "e.id = cast(? as uuid)"
      case None => "(1 = 1 or '' = ?)"
    }
    val dateFilter = date match {
      case Some(i) => "e.date >= ?"
      case None => "(1 = 1 or ? = e.date)"
    }
    val serviceTypeFilter = serviceType match {
      case Some(_) => "lower(s.type) = ?"
      case None => "(1 = 1 or ? = lower(s.type))"
    }
    val sql =
      s"""
         |select
         |       e.id,
         |       e.capacity,
         |       e.service_id,
         |       e.user_creator,
         |       e.date
         |from event e
         |left join service s
         |on e.service_id = s.id
         |where $serviceIdFilter
         |and $eventIdFilter
         |and $dateFilter
         |and $serviceTypeFilter
         |order by e.date desc
         |""".stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, serviceId.map(_.toString).getOrElse(""))
    ps.setString(2, eventId.map(_.toString).getOrElse(""))
    ps.setTimestamp(3, date.map(Timestamp.valueOf).getOrElse(Timestamp.valueOf(LocalDateTime.now())))
    ps.setString(4, serviceType.map(_.toString.toLowerCase()).getOrElse(""))

    val result = createSeq(ps.executeQuery(), eventCreator)
    result
  }


  private def eventCreator(rs: ResultSet): Event = {
    // FIXME handle nulls
    Event(
      rs.getString("id"),
      EventAttribute(
        rs.getString("service_id"),
        rs.getInt("capacity"),
        rs.getTimestamp("date").toLocalDateTime.toLocalDate,
        rs.getOptionalUUID("user_creator")
      )
    )
  }

  private def parseOptionUUID(uuid: String): Option[UUID] = {
    if (uuid == null){
      None
    } else {
      Option[UUID](uuid)
    }
  }

}
