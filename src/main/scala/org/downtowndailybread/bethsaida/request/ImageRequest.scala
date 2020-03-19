package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, Timestamp}
import java.time.LocalDateTime
import java.util.UUID

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.model.InternalUser
import org.downtowndailybread.bethsaida.providers.UUIDProvider
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}

class ImageRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {

  def createImage(key: Option[UUID])(implicit u: InternalUser): Unit = {

    val sql =
      s"""
         insert into image (id, created_by, created_time)
         values (cast(? as uuid), cast(? as uuid), ?)
        """

    val ps = conn.prepareStatement(sql)
    ps.setNullableUUID(1, key)
    ps.setString(2, u.id.toString)
    ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()))

    ps.executeUpdate()
  }

  def deleteImage(key: Option[UUID]): Unit = {
    key match {
      case Some(k) =>
        val sql =
          s"""
         delete from image
         where id = cast (? as uuid)
         """

        val ps = conn.prepareStatement(sql)
        ps.setString(1, k)
        ps.executeUpdate()
      case None =>
    }
  }

}
