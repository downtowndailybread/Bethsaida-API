package org.downtowndailybread.request

import java.sql.{Connection, ResultSet}

import org.downtowndailybread.exception.clientattributetype._
import org.downtowndailybread.model.{ClientAttributeType, ClientAttributeTypeAttribute}
import org.downtowndailybread.service.UUIDProvider


class ClientAttributeTypeRequest(val conn: Connection)
  extends DatabaseRequest
    with UUIDProvider {

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


  def getClientAttributeTypes(attributeName: Option[String] = None): Seq[ClientAttributeType] = {
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

  def getClientAttributeTypeByName(attribName: String): ClientAttributeType = {
    getClientAttributeTypes(Some(attribName)).toList match {
      case cat :: Nil => cat
      case _ => throw new ClientAttributeTypeNotFoundException(attribName)
    }
  }

  def insertClientAttributeType(cat: ClientAttributeType): Unit = {
    getClientAttributeTypes(Some(cat.id)).toList match {
      case Nil =>
        val catSql =
          s"""
             |insert into client_attribute_type (id, name, metadata_id)
             |VALUES (cast(? as uuid), ?, ?)
             |""".stripMargin

        val catPs = conn.prepareStatement(catSql)
        catPs.setString(1, getUUID().toString)
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

  def deleteClientAttributeType(attribName: String): Unit = {
    updateClientAttributeType(getClientAttributeTypeByName(attribName), false)
  }


  def updateClientAttributeType(cat: ClientAttributeType, isValid: Boolean = true): Unit = {

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
      case e: Exception => throw new ClientAttributeTypeInsertionErrorException(e)
    }
  }
}
