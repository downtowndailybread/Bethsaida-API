package org.downtowndailybread.request

import java.sql.Connection
import java.util.UUID

import org.downtowndailybread.exceptions.DDBException
import org.downtowndailybread.exceptions.client.{NoSuchClientException, NoSuchClientsException}
import org.downtowndailybread.model.{Client, ClientAttribute, ClientAttributeType}
import spray.json._

class ClientRequest(val conn: Connection) extends DatabaseRequest {

  val clientAttributeTypesRequester = new ClientAttributeTypeRequest(conn)

  def getClientsById(ids: Seq[UUID]): Seq[Client] = {
    val wildcardFilters =
      if (ids.isEmpty)
        "1=1 "
      else
        " cid.id in " + ids.map(_ => "cast(? as uuid)").mkString("(", ", ", ")")
    val sql =
      s"""
         |select * from (select distinct on (cid.id,
         |                                  cattribtype.id) cid.id as userId,
         |                                  cattribtype.id         as attribTypeId,
         |                                  cattribtype.name       as attribName,
         |                                  cattribtype.type       as attribType,
         |                                  cattribtype.required,
         |                                  cattribtype.ordering,
         |                                  cattrib.value,
         |                                  meta.is_valid
         |               from canonical_id cid
         |                           left join canonical_type ctype on cid.type = ctype.id
         |                           left join client_attribute cattrib on cid.id = cattrib.client_id
         |                           left join metadata meta on cattrib.metadata_id = meta.rid
         |                           left join client_attribute_type cattribtype on cattrib.type_id = cattribtype.id
         |               where 1 = 1
         |                 and ctype.type = 'client'
         |                 and $wildcardFilters
         |               order by cid.id, cattribtype.id, cattrib.rid desc) all_attribs
         |where is_valid
      """.stripMargin
    val statement = conn.prepareStatement(sql)

    ids.zipWithIndex.foreach { case (uuid, idx) => statement.setString(idx + 1, uuid.toString) }

    val result = statement.executeQuery()

    val attributes = createSeq(result, result => {
      val ctype = ClientAttributeType(
        result.getString("attribName"),
        result.getString("attribType"),
        result.getBoolean("required"),
        result.getInt("ordering")
      )
      (
        UUID.fromString(result.getString("userId")),
        ClientAttribute(ctype, s"""${result.getString("value")}""".parseJson)
      )
    }).groupBy(r => r._1).map { case (k, v) => (Some(k), v.map(_._2)) }


    val results = {
      if (ids.nonEmpty) {
        ids.map(id => (id, attributes.get(Some(id))))
      } else {
        attributes.toSeq.map { case (k, v) => (k.get, Some(v)) }
      }
    }

    if (results.exists(_._2.isEmpty)) {
      throw new NoSuchClientsException(results.filter(_._2.isEmpty).map(_._1))
    }
    else {
      results.map { case (id, attribs) => Client(id, attribs.get) }
    }
  }

  def getClientById(id: UUID): Client = {
    getClientsById(Seq(id)).toList match {
      case client :: Nil => client
      case Nil => throw new NoSuchClientException(id)
      case client :: rest => throw new DDBException("this code should not be reachable") {}

    }
  }


  def getAllClientInfo(): Seq[Client] = {
    getClientsById(Seq())
  }


  def insertClient(attribs: Seq[ClientAttribute]): UUID = {
    val clientId = insertCanonicalId(conn, Client)
    val attributeTypes = new ClientAttributeTypeRequest(conn).getClientAttributeTypes()
      .map { case (id, atype) => (atype.name, id) }.toMap
    val sql =
      """
        |insert into client_attribute
        |    (id, client_id, type_id, value, metadata_id)
        |VALUES (cast(? as uuid), cast(? as uuid), cast(? as uuid), cast(? as json), ?)
      """.stripMargin

    val ps = conn.prepareStatement(sql)
    attribs.foreach { attrib =>
      val attribId = insertCanonicalId(conn, ClientAttribute)
      val metaId = insertMetadataStatement(conn, true)
      ps.setString(1, attribId.toString)
      ps.setString(2, clientId.toString)
      ps.setString(3, attributeTypes(attrib.attributeType.name).toString)
      ps.setString(4, attrib.attributeValue.toString())
      ps.setInt(5, metaId)
      ps.addBatch()
    }

    val numResults = ps.executeUpdate()

    clientId
  }

  def updateClient(client: Client): UUID = {
    val existingClient = getClientById(client.id)
    val newAttributes = client.attributes.filter {
      attrib =>
        existingClient.attributes.find(_.attributeType == attrib.attributeType) match {
          case Some(eAttrib) => attrib.attributeValue != eAttrib.attributeValue
          case None => true
        }
    }
    val deletedAttributes = existingClient.attributes.filterNot(
      eAttrib => client.attributes.exists(_.attributeType == eAttrib.attributeType)
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
        .map { case (id, atype) => (atype.name, id) }.toMap

      deltas.foreach {
        case (attrib, valid) =>
          val attribId = insertCanonicalId(conn, ClientAttribute)
          val metaId = insertMetadataStatement(conn, valid)

          ps.setString(1, attribId.toString)
          ps.setString(2, client.id.toString)
          ps.setString(3, attributeTypes(attrib.attributeType.name).toString)
          ps.setString(4, attrib.attributeValue.toString())
          ps.setInt(5, metaId)
          ps.addBatch()
      }

      val numResults = ps.executeBatch()
    }

    client.id
  }
}
