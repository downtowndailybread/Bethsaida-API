package org.downtowndailybread.bethsaida

import org.downtowndailybread.bethsaida.request.DatabaseSource
import org.flywaydb.core.Flyway

object Migrate {

  def main(args: Array[String]): Unit = {
    val flyway = Flyway.configure.dataSource(DatabaseSource.ds).load()
    flyway.migrate()
  }
}
