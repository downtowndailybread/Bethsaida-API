package org.downtowndailybread.bethsaida.request.util

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.util.UUID

import org.downtowndailybread.bethsaida.model.{AnonymousUser, InternalUser}
import org.downtowndailybread.bethsaida.providers.UUIDProvider

trait DatabaseRequest {

  this: UUIDProvider =>

  def createSeq[E](rs: ResultSet, mapFunc: (ResultSet) => E): Seq[E] = {
    new Iterator[E] {
      override def hasNext: Boolean = rs.next()

      override def next(): E = mapFunc(rs)
    }.toSeq
  }

  class EnhancedPreparedStatement(ps: PreparedStatement) {
    def setNullableInt(parameterIndex: Int, x: Option[Int]): Unit = {
      x match {
        case Some(i) => ps.setInt(parameterIndex, i)
        case None => ps.setNull(parameterIndex, java.sql.Types.INTEGER)
      }
    }
    def setNullableString(parameterIndex: Int, x: Option[String]): Unit = {
      x match {
        case Some(i) => ps.setString(parameterIndex, i)
        case None => ps.setNull(parameterIndex, java.sql.Types.VARCHAR)
      }
    }
    def setNullableUUID(parameterIndex: Int, x: Option[UUID]): Unit = {
      x match {
        case Some(i) => ps.setString(parameterIndex, i)
        case None => ps.setNull(parameterIndex, java.sql.Types.VARCHAR)
      }
    }
  }

  implicit def toEnhancedPreparedStatement(ps: PreparedStatement): EnhancedPreparedStatement =
    new EnhancedPreparedStatement(ps)

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
      ps.setString(2, user.id)
      val rs = ps.executeQuery()
      rs.next()
      rs.getInt("rid")
    }
  }
}
