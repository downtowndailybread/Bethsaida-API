package org.downtowndailybread.bethsaida.json

import org.downtowndailybread.bethsaida.model.{ServiceMonthlyStats, SummaryStats}
import org.downtowndailybread.bethsaida.providers.SettingsProvider
import spray.json.DefaultJsonProtocol._

trait StatsJson extends BaseSupport {
    this: SettingsProvider =>

  implicit val serviceMonthlyStats = jsonFormat9(ServiceMonthlyStats)
  implicit val summaryStatsJson = jsonFormat4(SummaryStats)
}
