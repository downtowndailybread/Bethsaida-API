package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet}
import java.util.UUID

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
    getClientAttributeTypesInternal(None).values.toSeq
  }

  /**
    * Gets the specific client attribute type based on its name
    *
    * @param attribName the name of the attribute type to fetch
    * @return the client attribute type of the name given
    */
  def getClientAttributeTypeByName(attribName: String): ClientAttributeType = {
    getClientAttributeTypesInternal(Some(attribName)).toList match {
      case cat :: Nil => cat._2
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
             |insert into client_attribute_type
             |    (id, name, display_name, type, required, required_for_onboarding, metadata_id)
             |values (cast(? as uuid), ?, ?, ?, ?, ?, ?)
             |""".stripMargin

        val catPs = conn.prepareStatement(catSql)
        catPs.setString(1, getUUID())
        catPs.setString(2, cat.id)
        catPs.setString(3, cat.clientAttributeTypeAttribute.displayName)
        catPs.setString(4, cat.clientAttributeTypeAttribute.dataType)
        catPs.setBoolean(5, cat.clientAttributeTypeAttribute.required)
        catPs.setBoolean(6, cat.clientAttributeTypeAttribute.requiredForOnboarding)
        catPs.setInt(7, insertMetadataStatement(conn, true))

        try {
          if (catPs.executeUpdate() != 1) {
            throw new Exception("could not create client attrib type record")
          }
        }
        catch {
          case e: Exception => throw new ClientAttributeTypeInsertionErrorException(e)
        }
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
    val sql =
      s"""
         |delete from client_attribute_type
         |where name = ?
       """.stripMargin
    val ps = conn.prepareStatement(sql)
    ps.setString(1, attribName)
    ps.executeUpdate()
  }


  def updateClientAttributeType(cat: ClientAttributeType)(
    implicit au: InternalUser
  ): Unit = {

    val cata = cat.clientAttributeTypeAttribute
    val cataSql =
      s"""
         |update client_attribute_type
         |  set
         |    name = ?,
         |    display_name = ?,
         |    type = ?,
         |    required = ?,
         |    required_for_onboarding = ?,
         |    ordering = ?
         |where name = ?
           """.stripMargin

    val cataPs = conn.prepareStatement(cataSql)
    cataPs.setString(1, cat.id)
    cataPs.setString(2, cata.displayName)
    cataPs.setString(3, cata.dataType)
    cataPs.setBoolean(4, cata.required)
    cataPs.setBoolean(5, cata.requiredForOnboarding)
    cataPs.setInt(6, cata.ordering)
    cataPs.setString(7, cat.id)


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
  def getClientAttributeTypesInternal(attributeName: Option[String] = None): Map[UUID, ClientAttributeType] = {
    val filter = attributeName match {
      case Some(attribName) => s"name = '$attribName'"
      case None => "1=1"
    }
    val sql =
      s"""
         |select
         |  id,
         |  name,
         |  display_name,
         |  type,
         |  required,
         |  required_for_onboarding,
         |  ordering
         |from client_attribute_type
         |where $filter
      """.stripMargin
    createSeq(
      {
        val ps = conn.prepareStatement(sql)
        ps.executeQuery()
      },
      convertRs
    ).toMap
  }


  private def convertRs(res: ResultSet): (UUID, ClientAttributeType) = {
    (
      res.getString("id"),
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
    )
  }
}
