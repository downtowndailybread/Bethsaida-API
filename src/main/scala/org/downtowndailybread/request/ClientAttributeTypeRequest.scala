package org.downtowndailybread.request

import java.sql.{Connection, ResultSet}
import java.util.UUID

import org.downtowndailybread.exceptions.clientattributetype.ClientAttributeTypeAlreadyExistsException
import org.downtowndailybread.model.{ClientAttributeType, ClientAttributeTypeInternal}


class ClientAttributeTypeRequest(val conn: Connection) extends DatabaseRequest {

  private def convertRs(res: ResultSet): ClientAttributeTypeInternal = {
    ClientAttributeTypeInternal(UUID.fromString(res.getString("id")),
      ClientAttributeType(
        res.getString("name"),
        res.getString("display_name"),
        res.getString("type"),
        res.getBoolean("required"),
        res.getBoolean("required_for_onboarding"),
        res.getInt("ordering")
      )
    )
  }


  def getClientAttributeTypes(): Seq[ClientAttributeTypeInternal] = {
    val sql =
      """
        |select
        | catype.id,
        | catype.name,
        | catype.display_name,
        | catype.type,
        | catype.required,
        | catype.required_for_onboarding,
        | catype.ordering
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
    ).sortBy(r => (r.tpe.ordering, r.tpe.name))
  }

  def newClientAttributeType(cat: ClientAttributeType): UUID = {
    getClientAttributeTypes().find(_.tpe.name == cat.name) match {
      case None =>
        val canonicalId = insertCanonicalId(conn, ClientAttributeType)
        updateClientAttributeType(canonicalId, cat, true)
      case Some(c) =>
        throw new ClientAttributeTypeAlreadyExistsException(cat.name)
    }
  }

  def updateClientAttributeType(id: UUID, cat: ClientAttributeType, isValid: Boolean): UUID = {
    val metaId = insertMetadataStatement(conn, true)
    val sql =
      """
        |insert into client_attribute_type (
        |id, name, display_name, type, required, required_for_onboarding, metadata_id, ordering
        |)
        |values (cast(? as uuid), ?, ?, ?, ?, ?, ?, ?)
      """.stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, id.toString)
    ps.setString(2, cat.name)
    ps.setString(3, cat.displayName)
    ps.setString(4, cat.dataType)
    ps.setBoolean(5, cat.required)
    ps.setBoolean(6, cat.requiredForOnboarding)
    ps.setInt(7, metaId)
    ps.setInt(8, cat.ordering)

    ps.executeUpdate()

    id
  }
}
