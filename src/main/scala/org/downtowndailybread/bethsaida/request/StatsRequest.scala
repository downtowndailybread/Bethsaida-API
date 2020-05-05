package org.downtowndailybread.bethsaida.request

import java.sql.Connection

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.model.{ServiceMonthlyStats, SummaryStats}
import org.downtowndailybread.bethsaida.providers.UUIDProvider
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}

class StatsRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {

  def getSummaryStats(): SummaryStats = {
    SummaryStats(
      getNumberOfClients,
      getNumberOfAttendanceSheets,
      getNumberOfVisits,
      getMonthlyStats
    )
  }

  private def getMonthlyStats: Map[Int, Map[Int, Map[String, ServiceMonthlyStats]]] = {
    val ps = conn.prepareStatement(
      """
        |select
        |       s.name,
        |       date_part('year', att.check_in_time) as year,
        |       date_part('month', att.check_in_time) as month,
        |       count(distinct att.client_id) as num_clients,
        |       count(*) as total_visits,
        |       count(e.id) as num_events,
        |       count(case c.gender when 'male' then 1 else null end) as count_male,
        |       count(case c.gender when 'female' then 1 else null end) as count_female,
        |       count( case when
        |           (date_part('year', att.check_in_time) = date_part('year', c.intake_date) and
        |           date_part('month', att.check_in_time) = date_part('month', c.intake_date))
        |           then 1 else null end
        |           ) as current_month_intake
        |from attendance att
        |inner join client c on att.client_id = c.id
        |inner join event e on att.event_id = e.id
        |inner join service s on e.service_id = s.id
        |group by date_part('year', att.check_in_time),
        |         date_part('month', att.check_in_time),
        |         s.name;
        |
        |""".stripMargin
    )
    val rs = ps.executeQuery()
    val seq = createSeq(rs, rs => {
      ServiceMonthlyStats(
        rs.getString("name"),
        rs.getInt("year"),
        rs.getInt("month"),
        rs.getInt("num_clients"),
        rs.getInt("total_visits"),
        rs.getInt("num_events"),
        rs.getInt("count_male"),
        rs.getInt("count_female"),
        rs.getInt("current_month_intake")
      )
    })
    seq.groupBy(_.year).map {
      case(year, s) => (year, s.groupBy(_.month).map{
        case (month, t) => (month, t.groupBy(_.serviceName).map{
          case (name, u) => (name, t.head)
        })
      })
    }
  }


  private def getNumberOfClients: Int = {
    val ps = conn.prepareStatement(
      s"""
         |select count(*) from client""".stripMargin)
    val rs = ps.executeQuery()
    rs.next()
    rs.getInt(1)
  }

  private def getNumberOfAttendanceSheets: Int = {
    val ps = conn.prepareStatement(
      s"""
         |select count(*) from event""".stripMargin)
    val rs = ps.executeQuery()
    rs.next()
    rs.getInt(1)
  }

  private def getNumberOfVisits: Int = {
    val ps = conn.prepareStatement(
      s"""
         |select count(*) from attendance""".stripMargin)
    val rs = ps.executeQuery()
    rs.next()
    rs.getInt(1)
  }

}
