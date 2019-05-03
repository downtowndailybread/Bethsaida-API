package org.downtowndailybread.bethsaida.request.util

import java.sql.{Connection, ResultSet}

import org.downtowndailybread.bethsaida.model.{AnonymousUser, InternalUser}

trait DatabaseRequest {

  def createSeq[E](rs: ResultSet, mapFunc: (ResultSet) => E): Seq[E] = {
    new Iterator[E] {
      override def hasNext: Boolean = rs.next()

      override def next(): E = mapFunc(rs)
    }.toSeq
  }

  def insertMetadataStatement(connection: Connection, isValid: Boolean)
                             (implicit user: InternalUser): Int = {
    if(user == AnonymousUser) {
      val ps = connection.prepareStatement(
        s"""
           |insert into metadata (is_valid, when_entered)
           |VALUES (?, localtimestamp)
           |RETURNING rid
      """.stripMargin)
      ps.setBoolean(1, isValid)
      val rs = ps.executeQuery()
      rs.next()
      rs.getInt("rid")
    } else {
      val ps = connection.prepareStatement(
        s"""
           |insert into metadata (is_valid, when_entered, create_user)
           |VALUES (?, localtimestamp, cast(? as uuid))
           |RETURNING rid
      """.stripMargin)
      ps.setBoolean(1, isValid)
      ps.setString(2, user.id.toString)
      val rs = ps.executeQuery()
      rs.next()
      rs.getInt("rid")
    }
  }
}
