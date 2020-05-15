package org.downtowndailybread.bethsaida.json

import org.downtowndailybread.bethsaida.model.{ServiceDailyStats, ServiceMonthlyStats, SummaryStats}
import org.downtowndailybread.bethsaida.providers.SettingsProvider
import spray.json.DefaultJsonProtocol._

trait StatsJson extends BaseSupport {
    this: SettingsProvider =>

  implicit val serviceMonthlyStats = jsonFormat8(ServiceMonthlyStats)
  implicit val serviceDailyStats = jsonFormat9(ServiceDailyStats)
  implicit val serviceListMonthlyStats = seqFormat[ServiceMonthlyStats]
  implicit val serviceListDailyStats = seqFormat[ServiceDailyStats]
  implicit val summaryStatsJson = jsonFormat5(SummaryStats)
}
