package org.downtowndailybread.bethsaida.json

import org.downtowndailybread.bethsaida.model.{CovidStats, RaceStats, ServiceStats, SummaryStats}
import org.downtowndailybread.bethsaida.providers.SettingsProvider
import spray.json.DefaultJsonProtocol._

trait StatsJson extends BaseSupport {
    this: SettingsProvider =>

  implicit val serviceStats = jsonFormat12(ServiceStats)
  implicit val serviceListStats = seqFormat[ServiceStats]
  implicit val covidStats = jsonFormat2(CovidStats)
  implicit val raceStats = jsonFormat3(RaceStats)
  implicit val raceListStats = seqFormat[RaceStats]
  implicit val summaryStatsJson = jsonFormat8(SummaryStats)
}
