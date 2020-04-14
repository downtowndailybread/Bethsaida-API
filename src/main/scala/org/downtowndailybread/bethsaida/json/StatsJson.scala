package org.downtowndailybread.bethsaida.json

import org.downtowndailybread.bethsaida.model.SummaryStats
import org.downtowndailybread.bethsaida.providers.SettingsProvider
import spray.json.DefaultJsonProtocol._

trait StatsJson extends BaseSupport {
    this: SettingsProvider =>

  implicit val summaryStatsJson = jsonFormat3(SummaryStats)
}
