package org.downtowndailybread.request

import java.sql.Connection
import java.util.Properties

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

object DatabaseSource {

  private val config = {
    import java.io.PrintWriter
    val props = new Properties()
    props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource")
    props.setProperty("dataSource.user", "postgres")
    props.setProperty("dataSource.password", "docker")
    props.setProperty("dataSource.databaseName", "ddb")
    props.put("dataSource.logWriter", new PrintWriter(System.out))
    val config = new HikariConfig(props)
    config.setSchema("bethsaida")
    config
  }

  val ds = new HikariDataSource(config)


  def runSql[T](funct: (Connection) => T): T = {
    val connection = DatabaseSource.ds.getConnection()
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
