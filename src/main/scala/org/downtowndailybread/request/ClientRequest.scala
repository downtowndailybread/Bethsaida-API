package org.downtowndailybread.request

import java.sql.Connection
import java.util.UUID

import org.downtowndailybread.exceptions.client.ClientNotFoundException
import org.downtowndailybread.exceptions.clientattributetype.ClientAttributeTypeNotFoundException
import org.downtowndailybread.model.helper.AttribNameValuePair
import org.downtowndailybread.model.{Client, ClientAttribute, ClientAttributeTypeInternal}
import spray.json._

class ClientRequest(val conn: Connection) extends DatabaseRequest {

  val clientAttributeTypesRequester = new ClientAttributeTypeRequest(conn)

  def getAllClients(uuid: Option[UUID] = None): Seq[Client] = {
    val filter = uuid match {
      case Some(id) => "cid.id = ?"
      case None => "(1=1 or ''=?)"
    }
    val sql =
      s"""
         |select * from (select distinct on (cid.id,
         |                                  cattribtype.id) cid.id as userId,
         |                                  cattribtype.id         as attribTypeId,
         |                                  cattrib.value,
         |                                  meta.is_valid
         |               from canonical_id cid
         |                           left join canonical_type ctype on cid.type = ctype.id
         |                           left join client_attribute cattrib on cid.id = cattrib.client_id
         |                           left join metadata meta on cattrib.metadata_id = meta.rid
         |                           left join client_attribute_type cattribtype on cattrib.type_id = cattribtype.id
         |               where 1 = 1
         |                 and ctype.type = 'client'
         |                 and $filter
         |               order by cid.id, cattribtype.id, cattrib.rid desc) all_attribs
         |where is_valid
      """.stripMargin
    val statement = conn.prepareStatement(sql)
    statement.setString(1, uuid.map(_.toString).getOrElse(""))

    val result = statement.executeQuery()

    val clientAttributeTypes = (new ClientAttributeTypeRequest(conn)).getClientAttributeTypes()

    val clients = createSeq(result, result => {
      val attributeTypeName = result.getString("attribTypeId")
      clientAttributeTypes.find(_.id.toString == attributeTypeName) match {
        case Some(attrib) =>
          Client(
            UUID.fromString(result.getString("userId")),
            Seq(ClientAttribute(attrib.tpe, s"""${result.getString("value")}""".parseJson))
          )
        case None => // This code SHOULD be unreachable in practice, but in theory we could see it.
          throw new ClientAttributeTypeNotFoundException(attributeTypeName)
      }
    }).groupBy(r => r.id).map { case (k, v) => Client(k, v.flatMap(_.attributes)) }

    clients.toSeq
  }

  def getClientById(id: UUID): Client = {
    getAllClients(Some(id)) match {
      case client :: Nil => client
      case _ => throw new ClientNotFoundException(id)
    }
  }


  def insertClient(attribs: Seq[AttribNameValuePair]): Unit = {
    val clientId = insertCanonicalId(conn, Client)
    val attributeTypes = new ClientAttributeTypeRequest(conn).getClientAttributeTypes()
      .map { case ClientAttributeTypeInternal(id, atype) => (atype.name, id) }.toMap
    val sql =
      """
        |insert into client_attribute
        |    (id, client_id, type_id, value, metadata_id)
        |VALUES (cast(? as uuid), cast(? as uuid), cast(? as uuid), cast(? as json), ?)
      """.stripMargin

    val ps = conn.prepareStatement(sql)
    attribs.foreach { case AttribNameValuePair(attribName, attrib) =>
      val attribId = insertCanonicalId(conn, ClientAttribute)
      val metaId = insertMetadataStatement(conn, true)
      ps.setString(1, attribId.toString)
      ps.setString(2, clientId.toString)
      ps.setString(3, attributeTypes(attribName).toString)
      ps.setString(4, attrib.toString)
      ps.setInt(5, metaId)
      ps.addBatch()
    }

    val numResults = ps.executeBatch()
  }

  def updateClient(id: UUID, attribs: Seq[AttribNameValuePair]): Unit = {
    val existingClient = getClientById(id)
    val attributeType = new ClientAttributeTypeRequest(conn).getClientAttributeTypes()
    val newAttributes = attribs.map {
      case AttribNameValuePair(name, value) =>
        ClientAttribute(attributeType.find(_.tpe.name == name).get.tpe, value)
    }


    val deletedAttributes = existingClient.attributes.filterNot(
      eAttrib => newAttributes.exists(_.attributeType == eAttrib.attributeType)
    )

    val deltas = newAttributes.map(r => (r, true)) ++ deletedAttributes.map(r => (r, false))

    if (deltas.nonEmpty) {
      val sql =
        """
          |insert into client_attribute
          |    (id, client_id, type_id, value, metadata_id)
          |VALUES (cast(? as uuid), cast(? as uuid), cast(? as uuid), cast(? as json), ?)
        """.stripMargin

      val ps = conn.prepareStatement(sql)
      val attributeTypes = new ClientAttributeTypeRequest(conn).getClientAttributeTypes()

      deltas.foreach {
        case (attrib, valid) =>
          val attribId = insertCanonicalId(conn, ClientAttribute)
          val metaId = insertMetadataStatement(conn, valid)

          val attributeType = attributeTypes.find(_.tpe.name == attrib.attributeType.name) match {
            case Some(at) => at
            case None => throw new ClientAttributeTypeNotFoundException(attrib.attributeType.name)
          }

          ps.setString(1, attribId.toString)
          ps.setString(2, id.toString)
          ps.setString(3, attributeType.id.toString)
          ps.setString(4, attrib.attributeValue.toString())
          ps.setInt(5, metaId)
          ps.addBatch()
      }

      val numResults = ps.executeBatch()
    }
  }
}
