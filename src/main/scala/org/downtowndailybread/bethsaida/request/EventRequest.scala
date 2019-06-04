package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet}
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.event.EventNotFoundException
import org.downtowndailybread.bethsaida.model.{Event, EventAttribute, HoursOfOperation, InternalUser}
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}
import org.downtowndailybread.bethsaida.providers.{SettingsProvider, UUIDProvider}

class EventRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {

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
         |insert into event (id, start_time, end_time, capacity, service_id, schedule_creator, user_creator, name, metadata_id)
         |values (cast(? as uuid), ?, ?, ?, cast(? as uuid), cast(? as uuid), cast(? as uuid), ?, ?)
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, eventId)
    ps.setZonedDateTime(2, event.hours.start)
    ps.setZonedDateTime(3, event.hours.end)
    ps.setNullableInt(4, event.capacity)
    ps.setString(5, serviceId)
    ps.setNullableUUID(6, event.scheduleCreatorId)
    ps.setNullableUUID(7, event.userCreatorId)
    ps.setNullableString(8, None)
    ps.setInt(9, insertMetadataStatement(conn, true))

    ps.executeUpdate()
    eventId
  }

  def updateEvent(serviceId: UUID, eventId: UUID, event: EventAttribute)(implicit iu: InternalUser): Unit = {
    val sql =
      s"""
         |update event
         |    set start_time = ?,
         |        end_time = ?,
         |        capacity = ?,
         |        service_id = cast(? as uuid),
         |        schedule_creator = cast(? as uuid),
         |        user_creator = cast(? as uuid)
         |where id = cast(? as uuid)
       """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setZonedDateTime(1, event.hours.start)
    ps.setZonedDateTime(2, event.hours.end)
    ps.setNullableInt(3, event.capacity)
    ps.setString(4, serviceId)
    ps.setNullableUUID(5, event.scheduleCreatorId)
    ps.setNullableUUID(6, event.userCreatorId)
    ps.setString(7, eventId)

    ps.executeUpdate()
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
         |select
         |       id,
         |       start_time,
         |       end_time,
         |       capacity,
         |       service_id,
         |       schedule_creator,
         |       user_creator,
         |       name
         |from event
         |where $serviceIdFilter
         |and $eventIdFilter
       """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, serviceId.map(_.toString).getOrElse(""))
    ps.setString(2, eventId.map(_.toString).getOrElse(""))

    createSeq(ps.executeQuery(), eventCreator)
  }


  private def eventCreator(rs: ResultSet): Event = {
    Event(
      rs.getString("id"),
      rs.getString("service_id"),
      EventAttribute(
        HoursOfOperation(
          rs.getZoneDateTime("start_time"),
          rs.getZoneDateTime("end_time")
        ),
        Option(rs.getInt("capacity")),
        Option[UUID](rs.getString("user_creator")),
        Option[UUID](rs.getString("schedule_creator"))
      )
    )
  }

}
