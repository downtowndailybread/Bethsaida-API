package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.LocalDateTime
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.model.{Locker, LockerDetails, Mail, MailDetails}
import org.downtowndailybread.bethsaida.providers.UUIDProvider
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}

class MailRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {


  def getMail(): List[Mail] = {

    val sql =
      s"""
         |select m.id, m.client_id, m.start_date, m.end_date, m.input_user
         |from mail m
         |left join client c
         |on m.client_id = c.id
         |where end_date is null
         |and c.active
         |""".stripMargin


    val ps = conn.prepareStatement(sql)

    val result = ps.executeQuery()

    val rsConverter = (rs: ResultSet) => Mail(
      result.getUUID("id"),
      MailDetails(
        result.getUUID("client_id"),
        result.getLocalDateTime("start_date"),
        None,
        result.getUUID("input_user")
      ))

    val it = new Iterator[Mail] {
      override def hasNext: Boolean = result.next()

      override def next(): Mail = rsConverter(result)
    }

    it.toList
  }

  def removeMail(id: UUID, time: LocalDateTime): UUID = {
    val sql =
      s"""
         |update mail
         |set end_date = ?
         |where client_id = cast(? as uuid)""".stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setTimestamp(1, Timestamp.valueOf(time))
    ps.setUUID(2, id)
    ps.executeUpdate()

    id
  }


  def insertMail(mailDetails: MailDetails): UUID = {
    val id = UUID.randomUUID()
    val s =
      s"""
         |insert into mail (id, start_date, end_date, input_user, client_id)
         |values (cast(? as uuid), ?, ?, cast(? as uuid), cast(? as uuid))
         |""".stripMargin

    val ps = conn.prepareStatement(s)
    ps.setUUID(1, id)
    ps.setLocalDateTime(2, mailDetails.startDate)
    ps.setLocalDateTimeOption(3, mailDetails.endDate)
    ps.setUUID(4, mailDetails.inputUser)
    ps.setUUID(5, mailDetails.clientId)

    ps.executeUpdate()
    id
  }

}
