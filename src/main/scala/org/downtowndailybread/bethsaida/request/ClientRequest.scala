package org.downtowndailybread.bethsaida.request

import java.sql.Connection
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.client.{ClientInsertionErrorException, ClientNotFoundException, MissingRequiredClientAttributeException}
import org.downtowndailybread.bethsaida.exception.clientattributetype.ClientAttributeTypeNotFoundException
import org.downtowndailybread.bethsaida.model.{Client, ClientAttribute, InternalUser}
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}
import org.downtowndailybread.bethsaida.providers.{SettingsProvider, UUIDProvider}
import spray.json._

class ClientRequest(val conn: Connection, val settings: Settings)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider
    with SettingsProvider {

  val clientAttributeTypesRequester = new ClientAttributeTypeRequest(conn, settings)

  def getAllClients(uuid: Option[UUID] = None): Seq[Client] = {
    val filter = uuid match {
      case Some(id) => "c.id = cast(? as uuid)"
      case None => "(1=1 or ''=?)"
    }
    val sql =
      s"""
         |select userId, attribName, value
         |from (select distinct on (c.id,
         |                         cat.id) c.id      as userId,
         |                         cat.name          as attribName,
         |                         cattrib.value,
         |                         catMeta.is_valid  as attribTypeIsValid,
         |                         cmi.active        as userActive
         |      from client c
         |             left join client_attribute cattrib on c.id = cattrib.client_id
         |             left join client_attribute_type cat on cattrib.client_attribute_type_id = cat.id
         |             left join metadata catMeta on cattrib.metadata_id = catMeta.rid
         |             left join client_meta_info cmi on c.id = cmi.client_id
         |      where 1 = 1
         |        and $filter
         |      order by c.id, cat.id, cattrib.rid desc, cmi.rid desc) clients_attribs
         |where 1 = 1
         |  and attribTypeIsValid
         |  and userActive
      """.stripMargin
    val statement = conn.prepareStatement(sql)
    statement.setString(1, uuid.map(_.toString).getOrElse(""))

    val result = statement.executeQuery()

    val clientAttributeTypes = (new ClientAttributeTypeRequest(conn, settings)).getClientAttributeTypes()

    createSeq(result, r => {
      Client(r.getString("userId"),
        clientAttributeTypes.find(_.id == r.getString("attribName")) match {
          case Some(e) => Seq(ClientAttribute(e, r.getString("value").parseJson))
          case None => Seq()
        }
      )
    }).groupBy(_.id).map { case (id, clients) => Client(id, clients.flatMap(_.attributes)) }.toSeq
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
         |with row as (
         | insert into client (id, metadata_id)
         | values (cast(? as uuid), ?)
         | returning id
         |)
         |insert into client_meta_info (client_id, active)
         | values ((select id from row limit 1), true)
         |
      """.stripMargin

    val clientPs = conn.prepareStatement(sql)
    clientPs.setString(1, id)
    clientPs.setInt(2, clientMetadata)

    clientPs.executeUpdate()

    updateClient(id, attribs)

    id
  }

  def deleteClient(id: UUID): Unit = {
    getClientOptionById(id) match {
      case Some(client) =>
        val sql =
          s"""
        insert into client_meta_info (client_id, active)
        values (cast(? as uuid), false)
        """

        val ps = conn.prepareStatement(sql)
        ps.setString(1, id)
        ps.executeUpdate()
      case None => throw new ClientNotFoundException(id)
    }

  }

  def updateClient(
                    id: UUID,
                    attribs: Seq[ClientAttribute]
                  )(implicit au: InternalUser): Unit = {
    val existingClient = getClientOptionById(id)
    val attributeType = new ClientAttributeTypeRequest(conn, settings).getClientAttributeTypes()
    val newAttributes = attribs.map {
      case ClientAttribute(tpe, value) =>
        attributeType.find(_.id == tpe.id) match {
          case Some(cat) => ClientAttribute(cat, value)
          case None => throw new ClientAttributeTypeNotFoundException(tpe.id)
        }
    }

    val attribsContainsRequired = attributeType
      .filter(_.clientAttributeTypeAttribute.required)
      .map(at => (at.id, attribs.map(_.attributeType.id).contains(at.id)))
    if (!attribsContainsRequired.forall(_._2)) {
      throw new MissingRequiredClientAttributeException(attribsContainsRequired.filter(!_._2).map(_._1))
    }


    val deletedAttributes = existingClient.map(_.attributes.filterNot(
      eAttrib => newAttributes.exists(_.attributeType == eAttrib.attributeType)
    )).getOrElse(Seq())

    val deltas = newAttributes.map(r => (r, true)) ++ deletedAttributes.map(r => (r, false))

    if (deltas.nonEmpty) {
      val clientAttributeTypes = new ClientAttributeTypeRequest(conn, settings).getClientAttributeTypes()

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
        if cattrib.id == clientAttribute.attributeType.id
      } yield {
        val metaId = insertMetadataStatement(conn, include)

        aPs.setString(1, id)
        aPs.setString(2, clientAttribute.attributeType.id)
        aPs.setInt(3, metaId)
        aPs.setString(4, clientAttribute.attributeValue.toString)
        aPs.addBatch()
      }

      aPs.executeBatch()
    }
  }
}
