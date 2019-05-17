package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet}
import java.time.{OffsetDateTime, ZonedDateTime}
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.event.EventNotFoundException
import org.downtowndailybread.bethsaida.model.{Event, EventAttribute, HoursOfOperation, InternalUser}
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}
import org.downtowndailybread.bethsaida.providers.UUIDProvider

class EventRequest(conn: Connection, settings: Settings) extends BaseRequest
  with DatabaseRequest with UUIDProvider {

  def getAllServiceEvents(serviceId: UUID): Seq[Event] = {
    getAllEventsInternal(Some(serviceId), None)
  }

  def getEvent(serviceId: UUID, eventId: UUID): Event = {
    getAllEventsInternal(Some(serviceId), Some(eventId)) match {
      case e :: Nil => e
      case _ => throw new EventNotFoundException
    }
  }

  def createEvent(serviceId: UUID, event: EventAttribute)(implicit iu: InternalUser): UUID = {
    val eventId = getUUID()
    val sql =
      s"""
         |insert into event (id, service_id, metadata_id)
         |values (cast(? as uuid), cast(? as uuid), ?)
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, eventId)
    ps.setString(2, serviceId)
    ps.setInt(3, insertMetadataStatement(conn, true))
    insertEventAttributeInternal(eventId, event, true)
    eventId
  }

  def updateEvent(serviceId: UUID, eventId: UUID, event: EventAttribute)(implicit iu: InternalUser): Unit = {
    getEvent(serviceId, eventId)
    insertEventAttributeInternal(eventId, event, true)
  }

  def deleteEvent(serviceId: UUID, eventId: UUID)(implicit iu: InternalUser): Unit = {
    val event = getAllEventsInternal(Some(serviceId), Some(eventId)).toList match {
      case e :: Nil => e
      case _ => throw new EventNotFoundException
    }
    insertEventAttributeInternal(eventId, event.attribute, false)
  }

  def getAllEvents(): Seq[Event] = {
    getAllEventsInternal(None, None)
  }

  private def getAllEventsInternal(serviceId: Option[UUID], eventId: Option[UUID]): Seq[Event] = {
    val serviceIdFilter = serviceId match {
      case Some(i) => "e.service_id = cast(? as uuid)"
      case None => "(1 = 1 or '' = ?)"
    }
    val eventIdFilter = eventId match {
      case Some(i) => "e.id = cast(? as uuid)"
      case None => "(1 = 1 or '' = ?)"
    }
    val sql =
      s"""
         |select id, start_time, end_time, capacity, schedule_creator, user_creator
         |from (
         |         select distinct on (e.service_id) e.id,
         |                                           e.service_id,
         |                                           ea.start_time,
         |                                           ea.end_time,
         |                                           ea.capacity,
         |                                           ea.schedule_creator,
         |                                           ea.user_creator,
         |                                           m.is_valid
         |         from event e
         |             left join event_attribute ea on e.id = ea.event_id
         |                  left join metadata m on ea.metadata_id = m.rid
         |         where $serviceIdFilter
         |            and $eventIdFilter
         |         order by e.service_id, e.rid desc
         |     ) event
         |where event.is_valid
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, serviceId.map(_.toString).getOrElse(""))
    ps.setString(2, eventId.map(_.toString).getOrElse(""))

    createSeq(ps.executeQuery(), eventCreator)
  }

  private def insertEventAttributeInternal(eventId: UUID, event: EventAttribute, isValid: Boolean)(
    implicit iu: InternalUser
  ): Unit = {
    val metaId = insertMetadataStatement(conn, true)
    val sql =
      s"""
         |insert into event_attribute
         |    (event_id, start_time, end_time, capacity, schedule_creator, user_creator, metadata_id)
         |values
         |    (cast(? as uuid), ?, ?, ?, cast(? as uuid), cast(? as uuid), ?)
     """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, eventId)
    ps.setObject(2, event.hours.start.toOffsetDateTime)
    ps.setObject(3, event.hours.end.toOffsetDateTime)
    ps.setNullableInt(4, event.capacity)
    ps.setNullableUUID(5, event.scheduleCreatorId)
    ps.setNullableUUID(6, event.userCreatorId)
    ps.setInt(7, metaId)
  }

  private def getZoneDateTime(rs: ResultSet, col: String): ZonedDateTime = {
    val r = rs.getObject(col, classOf[OffsetDateTime])
    r.atZoneSameInstant(settings.timezone.toZoneId)
  }


  private def eventCreator(rs: ResultSet): Event = {
    Event(
      rs.getString("id"),
      rs.getString("service_id"),
      EventAttribute(
        HoursOfOperation(
          getZoneDateTime(rs, "start_time"),
          getZoneDateTime(rs, "end_time")
        ),
        Option(rs.getInt("capacity")),
        Option[UUID](rs.getString("user_creator")),
        Option[UUID](rs.getString("schedule_creator"))
      )
    )
  }

}
