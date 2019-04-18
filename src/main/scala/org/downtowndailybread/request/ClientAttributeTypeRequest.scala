package org.downtowndailybread.request

import java.sql.{Connection, ResultSet}
import java.util.UUID

import org.downtowndailybread.exceptions.client.ClientAttribTypeInsertionException
import org.downtowndailybread.model.ClientAttributeType


class ClientAttributeTypeRequest(val conn: Connection) extends DatabaseRequest {

  private def convertRs(res: ResultSet): (UUID, ClientAttributeType) = {
    (UUID.fromString(res.getString("id")),
      ClientAttributeType(
        res.getString("name"),
        res.getString("type"),
        res.getBoolean("required"),
        res.getInt("ordering")
      )
    )
  }


  def getClientAttributeTypes(): Seq[(UUID, ClientAttributeType)] = {
    val sql =
      """
        |select catype.id, catype.name, catype.type, catype.required, catype.ordering
        |from client_attribute_type catype
        |left join metadata meta on catype.metadata_id = meta.rid
        |where catype.rid in (select max(catype.rid) over (partition by cid.id)
        |                     from client_attribute_type catype
        |                            left join canonical_id cid on catype.id = cid.id
        |                            left join canonical_type ctype on cid.type = ctype.id
        |                     where ctype.type = ?)
        |and meta.is_valid
      """.stripMargin
    createSeq(
      {
        val ps = conn.prepareStatement(sql)
        ps.setString(1, ClientAttributeType.name)
        ps.executeQuery()
      },
      convertRs
    ).sortBy(r => (r._2.ordering, r._2.name))
  }


}
