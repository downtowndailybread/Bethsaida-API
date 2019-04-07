package org.downtowndailybread.request

import java.sql.{Connection, ResultSet}

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
}
