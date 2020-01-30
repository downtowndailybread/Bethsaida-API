package org.downtowndailybread.integration.base

import java.sql.Connection
import java.util.Properties

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

object TestDatabaseSource {

  lazy val ds = new HikariDataSource({
    config.setSchema("bethsaida")
    config
  })

  def dropAndAddSchema() = {
    val conn = new HikariDataSource(config).getConnection
    val ps1 = conn.prepareStatement("SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'bethsaida'")
    val rs = ps1.executeQuery()
    if(rs != null && rs.next()) {
      conn.prepareStatement("drop schema bethsaida cascade;").execute()
    }
    conn.prepareStatement("create schema bethsaida;").execute()
  }

  def runSql[T](funct: (Connection) => T): T = {
    val connection = TestDatabaseSource.ds.getConnection()
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

  private val config = {
    import java.io.PrintWriter
    val props = new Properties()
    props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource")
    props.setProperty("dataSource.user", "postgres")
    props.setProperty("dataSource.password", "docker")
    props.setProperty("dataSource.databaseName", "ddb_test")
    props.put("dataSource.logWriter", new PrintWriter(System.out))
    new HikariConfig(props)
  }
}
