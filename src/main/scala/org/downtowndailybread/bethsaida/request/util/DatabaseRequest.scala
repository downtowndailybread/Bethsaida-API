package org.downtowndailybread.bethsaida.request.util

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.time.{OffsetDateTime, ZonedDateTime}
import java.util.UUID

import org.downtowndailybread.bethsaida.exception.{DDBException, NoSuchIdException, TooManyRecordsFound}
import org.downtowndailybread.bethsaida.model.{AnonymousUser, InternalUser}
import org.downtowndailybread.bethsaida.providers.{SettingsProvider, UUIDProvider}

trait DatabaseRequest {

  this: UUIDProvider with SettingsProvider =>

  val noIdFound: Option[NoSuchIdException] = None
  val tooManyIdsFound: Option[TooManyRecordsFound] = None

  def createSeq[E](rs: ResultSet, mapFunc: ResultSet => E): Seq[E] = {
    new Iterator[E] {
      override def hasNext: Boolean = rs.next()

      override def next(): E = mapFunc(rs)
    }.toSeq
  }

  def getSingle[E](rs: ResultSet, mapFunc: ResultSet => E): E = {
    createSeq(rs, mapFunc) match {
      case Nil => throw noIdFound.get
      case h :: Nil => h
      case h :: _ => throw tooManyIdsFound.get
    }
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
    def setZonedDateTime(parameterIndex: Int, x: ZonedDateTime): Unit = {
      ps.setObject(parameterIndex, x.toOffsetDateTime)
    }
  }

  implicit def toEnhancedPreparedStatement(ps: PreparedStatement): EnhancedPreparedStatement =
    new EnhancedPreparedStatement(ps)

  class EnhancedResultSet(rs: ResultSet) {
    def getZoneDateTime(col: String): ZonedDateTime = {
      val r = rs.getObject(col, classOf[OffsetDateTime])
      r.atZoneSameInstant(settings.timezone.toZoneId)
    }
  }

  implicit def toEnhancedResultSet(rs: ResultSet): EnhancedResultSet =
    new EnhancedResultSet(rs)



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
