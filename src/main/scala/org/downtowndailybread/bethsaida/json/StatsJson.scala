package org.downtowndailybread.bethsaida.json

import org.downtowndailybread.bethsaida.model.{RaceStats, ServiceStats, SummaryStats}
import org.downtowndailybread.bethsaida.providers.SettingsProvider
import spray.json.DefaultJsonProtocol._

trait StatsJson extends BaseSupport {
    this: SettingsProvider =>

  implicit val serviceStats = jsonFormat9(ServiceStats)
  implicit val serviceListStats = seqFormat[ServiceStats]
  implicit val raceStats = jsonFormat3(RaceStats)
  implicit val raceListStats = seqFormat[RaceStats]
  implicit val summaryStatsJson = jsonFormat7(SummaryStats)
}
