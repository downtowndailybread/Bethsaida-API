package org.downtowndailybread.request

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.util.UUID

import org.downtowndailybread.model.CanonicalDataType


trait DatabaseRequest {

  def createSeq[E](rs: ResultSet, mapFunc: (ResultSet) => E): Seq[E] = {
    new Iterator[E] {
      override def hasNext: Boolean = rs.next()

      override def next(): E = mapFunc(rs)
    }.toSeq
  }

  def insertMetadataStatement(connection: Connection, isValid: Boolean): Int = {
    val rs = connection.prepareStatement(
      s"""
         |insert into metadata (is_valid, when_entered, create_user)
         |VALUES ($isValid, localtimestamp, 1)
         |RETURNING rid
      """.stripMargin).executeQuery()
    rs.next()
    rs.getInt("rid")
  }

  def insertCanonicalId(conn: Connection, canonicalType: CanonicalDataType): UUID = {
    val uuid = UUID.randomUUID()
    val metadataId = insertMetadataStatement(conn, true)
    val ps = conn.prepareStatement(
      s"""
         |insert into canonical_id (id, type, metadata_id)
         |values (cast(? as uuid), (select id
         |from canonical_type ct
         |            left join metadata meta on ct.metadata_id = meta.rid
         |where 1 = 1
         |  and ct.rid = (select distinct on (ct.type) ct.rid
         |                from canonical_type ct
         |                where ct.type = ?
         |                order by ct.type, ct.rid desc)
         |  and meta.is_valid), ?);
            """.stripMargin)
    ps.setString(1, uuid.toString)
    ps.setString(2, canonicalType.name)
    ps.setInt(3, metadataId)
    ps.execute()
    uuid
  }
}
