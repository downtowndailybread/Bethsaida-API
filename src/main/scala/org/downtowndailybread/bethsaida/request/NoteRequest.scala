package org.downtowndailybread.bethsaida.request

import java.sql.Connection
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.model.Note
import org.downtowndailybread.bethsaida.providers.UUIDProvider
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}

class NoteRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {


  def getNote(id: UUID): Option[Note] = {

    val sql =
      s"""
         |select *
         |from note
         |where id = cast(? as uuid)
         |""".stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setUUID(1, id)

    val result = ps.executeQuery()

    if(result.next()) {
      Some(Note(result.getString("note")))
    } else {
      None
    }
  }

  def setNote(id: UUID, note: Note): Unit = {
    val s =
      s"""
         |insert into note (id, note)
         |values (cast(? as uuid), ?)
         |on conflict (id)
         |do
         |update set note = EXCLUDED.note
         |""".stripMargin

    val ps = conn.prepareStatement(s)
    ps.setUUID(1, id)
    ps.setString(2, note.note)

    ps.executeUpdate()
  }
}
