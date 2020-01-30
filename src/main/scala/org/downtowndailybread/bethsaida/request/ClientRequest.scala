package org.downtowndailybread.bethsaida.request

import java.sql.Connection
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.client.{ClientInsertionErrorException, ClientNotFoundException, MissingRequiredClientAttributeException}
import org.downtowndailybread.bethsaida.exception.clientattributetype.ClientAttributeTypeNotFoundException
import org.downtowndailybread.bethsaida.model.{Client, ClientAttribute, ClientAttributeType, ClientAttributeTypeAttribute, InternalUser}
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}
import org.downtowndailybread.bethsaida.providers.{SettingsProvider, UUIDProvider}
import spray.json._


class ClientRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {

  val clientAttributeTypesRequester = new ClientAttributeTypeRequest(settings, conn)

  def getAllClients(uuid: Option[UUID] = None): Seq[Client] = {
    val filter = uuid match {
      case Some(id) => "c.id = cast(? as uuid)"
      case None => "(1=1 or ''=?)"
    }
    val sql =
      s"""
         |select
         |       c.id,
         |       cat.id as cat_id,
         |       cat.name,
         |       cat.display_name,
         |       cat.ordering,
         |       cat.required,
         |       cat.required_for_onboarding,
         |       cat.type,
         |       ca.value
         |from client c
         |         inner join client_attribute ca
         |                    on c.id = ca.client_id
         |         inner join client_attribute_type cat
         |                    on ca.client_attribute_type_id = cat.id
         |where c.active
         |and $filter
      """.stripMargin
    val statement = conn.prepareStatement(sql)
    statement.setString(1, uuid.map(_.toString).getOrElse(""))

    val result = statement.executeQuery()

    createSeq(result, {
      rs =>
        (
          rs.getUUID("id"),
          ClientAttribute(
            rs.getString("name"),
            rs.getString("value").parseJson
          )
        )
    }).groupBy(_._1).map {
      case (id, attribs) => Client(id, attribs.map(_._2))
    }.toSeq
  }

  private def getClientOptionById(id: UUID): Option[Client] = {
    getAllClients(Some(id)).headOption
  }

  def getClientById(id: UUID): Client = {
    getClientOptionById(id) match {
      case Some(client) => client
      case None => throw new ClientNotFoundException(id)
    }
  }


  def insertClient(attribs: Seq[ClientAttribute])(implicit au: InternalUser): UUID = {
    val clientMetadata = insertMetadataStatement(conn, true)
    val id = getUUID()
    val sql =
      s"""
         |insert into client
         | (id, active, metadata_id)
         |VALUES (cast(? as uuid), true, ?)
         |""".stripMargin

    val ps = conn.prepareStatement(sql)
    ps.setString(1, id)
    ps.setInt(2, clientMetadata)
    ps.executeUpdate()

    upsertAttributes(id, attribs)
    id
  }

  def upsertAttributes(id: UUID, attribs: Seq[ClientAttribute])(implicit au: InternalUser) = {
    val clientAttributeTypes = new ClientAttributeTypeRequest(settings, conn).getClientAttributeTypesInternal(None)
    val attribSql =
      s"""
         |INSERT INTO client_attribute
         |    (client_id, client_attribute_type_id, metadata_id, value)
         |values (cast(? as uuid), cast(? as uuid), ?, cast(? as json))
         |ON CONFLICT ON CONSTRAINT client_attribute_client_and_type_unique_constraint
         |DO UPDATE SET value = EXCLUDED.value
      """.stripMargin
    val clientPs = conn.prepareStatement(attribSql)

    attribs.foreach {
      attrib =>
        clientPs.setString(1, id)
        clientPs.setString(2,
          clientAttributeTypes.find(_._2.id == attrib.attributeName).map(_._1).get)
        clientPs.setInt(3, insertMetadataStatement(conn, true))
        clientPs.setString(4, attrib.attributeValue.toString())
        clientPs.addBatch()
    }
    try{
      clientPs.executeBatch()
    } catch {
      case e: Exception =>
    }

  }

  def deleteClient(id: UUID): Unit = {
    val sql =
      s"""
        delete from client
        where id = cast(? as uuid)
        cascade;
        """
    val ps = conn.prepareStatement(sql)
    ps.setString(1, id)
    ps.executeUpdate()
  }

  def updateClient(
                    id: UUID,
                    attribs: Seq[ClientAttribute]
                  )(implicit au: InternalUser): Unit = {
    val existingClient = getClientOptionById(id)
    val attributeType = new ClientAttributeTypeRequest(settings, conn).getClientAttributeTypes()
    val newAttributes = attribs.map {
      case ClientAttribute(tpe, value) =>
        attributeType.find(_.id == tpe) match {
          case Some(cat) => ClientAttribute(cat.id, value)
          case None => throw new ClientAttributeTypeNotFoundException(tpe)
        }
    }

    val attribsContainsRequired = attributeType
      .filter(_.clientAttributeTypeAttribute.required)
      .map(at => (at.id, attribs.map(_.attributeName).contains(at.id)))
    if (!attribsContainsRequired.forall(_._2)) {
      throw new MissingRequiredClientAttributeException(attribsContainsRequired.filter(!_._2).map(_._1))
    }


    val deletedAttributes = existingClient.map(_.attributes.filterNot(
      eAttrib => newAttributes.exists(_.attributeName == eAttrib.attributeName)
    )).getOrElse(Seq())

    val deltas = newAttributes.map(r => (r, true)) ++ deletedAttributes.map(r => (r, false))

    if (deltas.nonEmpty) {
      val clientAttributeTypes = new ClientAttributeTypeRequest(settings, conn).getClientAttributeTypes()

      val attribSql =
        s"""
           |insert into client_attribute
           |    (client_id, client_attribute_type_id, metadata_id, value)
           |values (cast(? as uuid), (select id from client_attribute_type where name = ?), ?, cast(? as json))
       """.stripMargin
      val aPs = conn.prepareStatement(attribSql)

      for {
        (clientAttribute, include) <- deltas
        cattrib <- clientAttributeTypes
        if cattrib.id == clientAttribute.attributeName
      } yield {
        val metaId = insertMetadataStatement(conn, include)

        aPs.setString(1, id)
        aPs.setString(2, clientAttribute.attributeName)
        aPs.setInt(3, metaId)
        aPs.setString(4, clientAttribute.attributeValue.toString)
        aPs.addBatch()
      }

      aPs.executeBatch()
    }
  }
}
