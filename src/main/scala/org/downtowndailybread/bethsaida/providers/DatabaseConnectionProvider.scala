package org.downtowndailybread.bethsaida.providers

import java.sql.Connection

trait DatabaseConnectionProvider {
  this: SettingsProvider =>


//  def runSql[T](funct: (Connection) => T): T = {
//    val connection = settings.ds.getConnection()
//    try {
//      val result = funct(connection)
//      result
//    } catch {
//      case e: Exception =>
//        throw e
//    } finally {
//      connection.close()
//    }
//  }

  def runSql[T](funct: (Connection) => T): T = {
    val connection = settings.ds.getConnection()
    connection.setAutoCommit(false)
    try {
      val result = funct(connection)
      connection.commit()
      result
    } catch {
      case e: Exception =>
        connection.rollback()
        throw e
    } finally {

      connection.setAutoCommit(true)
      connection.close()

    }
  }
}
