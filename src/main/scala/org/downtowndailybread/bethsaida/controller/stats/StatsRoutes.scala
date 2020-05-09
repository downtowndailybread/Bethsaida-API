package org.downtowndailybread.bethsaida.controller.stats

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.StatsRequest
import spray.json._


trait StatsRoutes extends ControllerBase {
  this: JsonSupport with AuthenticationProvider with SettingsProvider with DatabaseConnectionProvider =>

  private val summaryStats = path("summary") {
    authorizeNotAnonymous {
      implicit user =>
        get {
          futureComplete {
            val s = runSql(conn => new StatsRequest(settings, conn).getSummaryStats())
            val t = s.monthlyStats
            //val c = t.toList.flatMap(_._2.toList.flatMap(_._2.toList.map(_._2)))
            val c = t.toList.flatMap(_._2.toList)
            println(t)
            s
          }
        }
    }
  }

  lazy val allStatsRoutes = pathPrefix("stats") {
    summaryStats
  }


}
