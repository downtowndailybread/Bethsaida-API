package org.downtowndailybread.bethsaida.json

import org.downtowndailybread.bethsaida.model.{ServiceMonthlyStats, SummaryStats}
import org.downtowndailybread.bethsaida.providers.SettingsProvider
import spray.json.DefaultJsonProtocol._
import spray.json.{JsObject, JsValue, RootJsonFormat}

trait StatsJson extends BaseSupport {
    this: SettingsProvider =>

  implicit val serviceMonthlyStats = jsonFormat9(ServiceMonthlyStats)
  implicit val summaryStatsJson = new RootJsonFormat[SummaryStats] { // jsonFormat4(SummaryStats)
    override def read(json: JsValue): SummaryStats = ???

    override def write(obj: SummaryStats): JsValue = JsObject(
      Map(
        "monthlyStats" -> JsObject(obj.monthlyStats.map {
          case (year, yearRest) => (year.toString -> JsObject(yearRest.map {
            case (month, monthRest) => (month.toString -> JsObject(monthRest.map {
              case (service, stats) => (service -> serviceMonthlyStats.write(stats))
            }))
          }))
        })
      )
    )
  }
}
