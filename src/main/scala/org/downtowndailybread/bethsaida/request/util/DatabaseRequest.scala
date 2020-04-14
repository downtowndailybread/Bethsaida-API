package org.downtowndailybread.bethsaida.request.util

import java.sql.{PreparedStatement, ResultSet, Timestamp, Types}
import java.time.{LocalDate, LocalDateTime, ZonedDateTime}
import java.util.UUID

import org.downtowndailybread.bethsaida.exception.{NoSuchIdException, TooManyRecordsFound}
import org.downtowndailybread.bethsaida.providers.{SettingsProvider, UUIDProvider}

trait DatabaseRequest {

  this: UUIDProvider with SettingsProvider =>

  val noIdFound: Option[NoSuchIdException] = None
  val tooManyIdsFound: Option[TooManyRecordsFound] = None

  def createSeq[E](rs: ResultSet, mapFunc: ResultSet => E): Seq[E] = {
    new Iterator[E] {
      override def hasNext: Boolean = {
        !rs.isClosed && rs.next()
      }

      override def next(): E = mapFunc(rs)
    }.toList
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

    def setUUID(parameterIndex: Int, x: UUID): Unit = {
      ps.setString(parameterIndex, x.toString)
    }

    def setNullableTimestamp(parameterIndex: Int, x: Option[Timestamp]): Unit = {
      x match {
        case Some(i) => ps.setTimestamp(parameterIndex, i)
        case None => ps.setNull(parameterIndex, java.sql.Types.TIMESTAMP)
      }
    }
    def setZonedDateTime(parameterIndex: Int, x: ZonedDateTime): Unit = {
      ps.setObject(parameterIndex, x.toOffsetDateTime, Types.TIMESTAMP_WITH_TIMEZONE)
    }
  }

  implicit def toEnhancedPreparedStatement(ps: PreparedStatement): EnhancedPreparedStatement =
    new EnhancedPreparedStatement(ps)

  class EnhancedResultSet(rs: ResultSet) {

    def getOptionalInt(col: String): Option[Int] = {
      Option(rs.getInt(col))
    }

    def getOptionalString(col: String): Option[String] = {
      Option(rs.getString(col))
    }

    def getOptionalUUID(col: String): Option[UUID] = {
      getOptionalString(col).map(parseUUID)
    }

    def getUUID(col: String): UUID = {
      parseUUID(rs.getString(col))
    }

    def getLocalDate(col: String): LocalDate = {
      getLocalDateTime(col).toLocalDate
    }

    def getLocalDateTime(col: String): LocalDateTime = {
      rs.getTimestamp(col).toLocalDateTime
    }

    def getOptionalLocalDate(col: String): Option[LocalDate] = {
      getOptionalLocalDateTime(col).map(_.toLocalDate)
    }

    def getOptionalLocalDateTime(col: String): Option[LocalDateTime] = {
      Option(rs.getTimestamp(col)).map(_.toLocalDateTime)
    }
  }

  implicit def toEnhancedResultSet(rs: ResultSet): EnhancedResultSet =
    new EnhancedResultSet(rs)


}
