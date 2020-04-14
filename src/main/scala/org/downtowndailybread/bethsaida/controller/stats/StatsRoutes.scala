package org.downtowndailybread.bethsaida.controller.stats

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.StatsRequest

trait StatsRoutes extends ControllerBase{
  this: JsonSupport with AuthenticationProvider with SettingsProvider with DatabaseConnectionProvider =>

  private val summaryStats = path("summary") {
//    authorizeNotAnonymous {
//      implicit user =>
      get {
        futureComplete(runSql(conn => new StatsRequest(settings, conn).getSummaryStats()))
      }
//    }
  }

  lazy val allStatsRoutes = pathPrefix("stats") {
    summaryStats
  }


}
