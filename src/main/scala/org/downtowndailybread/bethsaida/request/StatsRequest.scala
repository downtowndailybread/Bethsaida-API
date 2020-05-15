package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet}

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.model.{ServiceDailyStats, ServiceMonthlyStats, SummaryStats}
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
      getMonthlyStats,
      getDailyStats
    )
  }

  private def getDailyStats: List[ServiceDailyStats] = {
    val ps = conn.prepareStatement(
      """
        |select
        |       s.name,
        |       date_part('year', e.date) as year,
        |       date_part('month', e.date) as month,
        |       date_part('day', e.date) as day,
        |       count(distinct att.client_id) as num_clients,
        |       count(*) as total_visits,
        |       count(e.id) as num_events,
        |       count(case c.gender when 'male' then 1 else null end) as count_male,
        |       count(case c.gender when 'female' then 1 else null end) as count_female
        |from attendance att
        |inner join client c on att.client_id = c.id
        |inner join event e on att.event_id = e.id
        |inner join service s on e.service_id = s.id
        |group by date_part('year', e.date),
        |         date_part('month', e.date),
        |         date_part('day', e.date),
        |         s.name
        |
        |""".stripMargin
    )

    val rs = ps.executeQuery()

    val rsConverter = (rs: ResultSet) => {
      val year = rs.getString("year").toInt
      val month = rs.getString("month").toInt
      val day = rs.getString("day").toInt
      ServiceDailyStats(
        rs.getString("name"),
        year,
        month,
        day,
        rs.getInt("num_clients"),
        rs.getInt("total_visits"),
        rs.getInt("num_events"),
        rs.getInt("count_male"),
        rs.getInt("count_female")
      )
    }

    val seq = createSeq(rs, rsConverter)

    seq.toList

  }

  private def getMonthlyStats: List[ServiceMonthlyStats] = {
    val intakeps = conn.prepareStatement(
      """
        |select
        |'intake' as name,
        |date_part('year', c.intake_date) as year,
        |date_part('month', c.intake_date) as month,
        |count(*) as num_clients,
        |count(*) as total_visits,
        |0 as num_events,
        |count(case c.gender when 'male' then 1 else null end) as count_male,
        |count(case c.gender when 'female' then 1 else null end) as count_female
        |from client c
        |group by date_part('year', c.intake_date), date_part('month', c.intake_date)
        |           """.stripMargin)
    val intakeRs = intakeps.executeQuery()

    val ps = conn.prepareStatement(
      """
        |select
        |       s.name,
        |       date_part('year', e.date) as year,
        |       date_part('month', e.date) as month,
        |       count(distinct att.client_id) as num_clients,
        |       count(*) as total_visits,
        |       count(e.id) as num_events,
        |       count(case c.gender when 'male' then 1 else null end) as count_male,
        |       count(case c.gender when 'female' then 1 else null end) as count_female
        |from attendance att
        |inner join client c on att.client_id = c.id
        |inner join event e on att.event_id = e.id
        |inner join service s on e.service_id = s.id
        |group by date_part('year', e.date),
        |         date_part('month', e.date),
        |         s.name
        |
        |""".stripMargin
    )
    val rs = ps.executeQuery()
    val rsConverter = (rs: ResultSet) => {
      val year = rs.getString("year").toInt
      val month = rs.getString("month").toInt
      ServiceMonthlyStats(
        rs.getString("name"),
        year,
        month,
        rs.getInt("num_clients"),
        rs.getInt("total_visits"),
        rs.getInt("num_events"),
        rs.getInt("count_male"),
        rs.getInt("count_female")
      )
    }

    val seq = createSeq(rs, rsConverter) ++ createSeq(intakeRs, rsConverter)

    seq.toList
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
