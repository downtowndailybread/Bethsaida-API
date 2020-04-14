package org.downtowndailybread.bethsaida

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway

object Migrate {

  def main(args: Array[String]): Unit = {
    val settings = new Settings(args)
    migrate(settings.ds)
  }

  def migrate(ds: HikariDataSource): Unit = {
    val flyway = Flyway.configure.dataSource(ds).schemas("bethsaida").load()
    flyway.migrate()
  }
}
