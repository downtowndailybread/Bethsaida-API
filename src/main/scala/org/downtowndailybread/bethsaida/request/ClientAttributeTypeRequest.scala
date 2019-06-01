package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet}

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.exception.clientattributetype._
import org.downtowndailybread.bethsaida.model.{ClientAttributeType, ClientAttributeTypeAttribute, InternalUser}
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}
import org.downtowndailybread.bethsaida.providers.{SettingsProvider, UUIDProvider}


class ClientAttributeTypeRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {

  /**
    * Returns all client attribute types
    *
    * @return all client attribute types
    */
  def getClientAttributeTypes(): Seq[ClientAttributeType] = {
    getClientAttributeTypesInternal(None)
  }

  /**
    * Gets the specific client attribute type based on its name
    *
    * @param attribName the name of the attribute type to fetch
    * @return the client attribute type of the name given
    */
  def getClientAttributeTypeByName(attribName: String): ClientAttributeType = {
    getClientAttributeTypesInternal(Some(attribName)).toList match {
      case cat :: Nil => cat
      case _ => throw new ClientAttributeTypeNotFoundException(attribName)
    }
  }

  /**
    * Inserts a client attribute type to the database
    *
    * @param cat the client attribute type
    * @param au  the user inserting the client attribute type
    */
  def insertClientAttributeType(cat: ClientAttributeType)(implicit au: InternalUser): Unit = {
    getClientAttributeTypesInternal(Some(cat.id)).toList match {
      case Nil =>
        val catSql =
          s"""
             |insert into client_attribute_type (id, name, metadata_id)
             |VALUES (cast(? as uuid), ?, ?)
             |""".stripMargin

        val catPs = conn.prepareStatement(catSql)
        catPs.setString(1, getUUID())
        catPs.setString(2, cat.id)
        catPs.setInt(3, insertMetadataStatement(conn, true))

        try {
          if (catPs.executeUpdate() != 1) {
            throw new Exception("could not create client attrib type record")
          }
        }
        catch {
          case e: Exception => throw new ClientAttributeTypeInsertionErrorException(e)
        }

        updateClientAttributeType(cat, true)
      case _ =>
        throw new ClientAttributeTypeAlreadyExistsException(cat.id)
    }
  }

  /**
    *
    * @param attribName
    * @param au
    */
  def deleteClientAttributeType(attribName: String)(
    implicit au: InternalUser
  ): Unit = {
    updateClientAttributeType(getClientAttributeTypeByName(attribName), false)
  }


  def updateClientAttributeType(cat: ClientAttributeType, isValid: Boolean = true)(
    implicit au: InternalUser
  ): Unit = {

    val cataSql =
      s"""
         |insert into client_attribute_type_attrib  (client_attribute_type_id,
         |                                           display_name,
         |                                           type,
         |                                           required,
         |                                           required_for_onboarding,
         |                                           ordering,
         |                                           metadata_id)
         |values ((select id from client_attribute_type where name = ? limit 1), ?, ?, ? ,?, ?, ?)
           """.stripMargin

    val cataPs = conn.prepareStatement(cataSql)
    cataPs.setString(1, cat.id)
    cataPs.setString(2, cat.clientAttributeTypeAttribute.displayName)
    cataPs.setString(3, cat.clientAttributeTypeAttribute.dataType)
    cataPs.setBoolean(4, cat.clientAttributeTypeAttribute.required)
    cataPs.setBoolean(5, cat.clientAttributeTypeAttribute.requiredForOnboarding)
    cataPs.setInt(6, cat.clientAttributeTypeAttribute.ordering)
    cataPs.setInt(7, insertMetadataStatement(conn, isValid))


    try {
      if (cataPs.executeUpdate() != 1) {
        throw new Exception("could not create client attrib type data record")
      }
    }
    catch {
      case e: Exception => {
        val f = e
        throw new ClientAttributeTypeInsertionErrorException(e)
      }
    }
  }

  /**
    * Gets client attribute types. If the attributeName is set to None, then this returns all attributetypes, otherwise
    * it returns the one corresponding to the name given in the Option.
    *
    * @param attributeName the optional name of the attribute
    * @return a sequence of matching ClientAttributeTypes
    */
  private def getClientAttributeTypesInternal(attributeName: Option[String] = None): Seq[ClientAttributeType] = {
    val filter = attributeName match {
      case Some(attribName) => s"cat.name = '$attribName'"
      case None => "1=1"
    }
    val sql =
      s"""
         |select *
         |from (select distinct on (cat.id) cat.name,
         |                         cata.display_name,
         |                         cata.required,
         |                         cata.required_for_onboarding,
         |                         cata.type,
         |                         cata.ordering,
         |                         meta.is_valid
         |      from client_attribute_type cat
         |             left join client_attribute_type_attrib cata on cat.id = cata.client_attribute_type_id
         |             left join metadata meta on cata.metadata_id = meta.rid
         |      where 1=1
         |             and $filter
         |      order by cat.id, cata.rid desc) attribs
         |where attribs.is_valid
      """.stripMargin
    createSeq(
      {
        val ps = conn.prepareStatement(sql)
        ps.executeQuery()
      },
      convertRs
    ).sortBy(r => (r.clientAttributeTypeAttribute.ordering, r.id)).toList
  }


  private def convertRs(res: ResultSet): ClientAttributeType = {
    ClientAttributeType(
      res.getString("name"),
      ClientAttributeTypeAttribute(
        res.getString("display_name"),
        res.getString("type"),
        res.getBoolean("required"),
        res.getBoolean("required_for_onboarding"),
        res.getInt("ordering")
      )
    )
  }
}
