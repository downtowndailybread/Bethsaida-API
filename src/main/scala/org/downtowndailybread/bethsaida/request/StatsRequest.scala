package org.downtowndailybread.bethsaida.request

import java.sql.{Connection, ResultSet}
import java.time.LocalDate

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.model.{RaceStats, ServiceStats, SummaryStats}
import org.downtowndailybread.bethsaida.providers.UUIDProvider
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}

class StatsRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {


  val currentKey = "current_total"
  val previousKey = "prev_total"
  val projectedKey = "projected_total"

  def getSummaryStats(): SummaryStats = {
    SummaryStats(
      getNumberOfClients,
      getNumberOfAttendanceSheets,
      getNumberOfVisits,
      getMonthlyStats,
      getDailyStats,
      getYearlyStats,
      getYearlyRaceStats,
    )
  }

  private def getYearlyRaceStats: List[RaceStats] = {
    val ps = conn.prepareStatement(
      s"""
         |
         |    select
         |       case when date_part('year', e.date) = date_part('year', current_date) then '$currentKey' else '$previousKey' end as name,
         |       c.race, count(*) as total
         |        from attendance att
         |    inner join client c on att.client_id = c.id
         |    inner join event e on att.event_id = e.id
         |    where date_part('year', e.date) between date_part('year', current_date) - 1 and date_part('year', current_date)
         |    group by date_part('year', e.date), c.race
         |""".stripMargin)

    val rs = ps.executeQuery()

    val rsConverter = (rs: ResultSet) => {
      RaceStats(
        rs.getString("name"),
        rs.getString("race"),
        rs.getInt("total")
      )
    }

    val seq = createSeq(rs, rsConverter)
    val factor = getFactor()
    seq.flatMap{
      item =>
        if (item.name == currentKey) {
          List(
            item,
            item.copy(name = projectedKey, count = Math.round(item.count * factor).toInt)
          )
        } else {
          List(item)
        }
    }
    seq.toList
  }

  private def getYearlyStats: List[ServiceStats] = {
    val ps = conn.prepareStatement(
      s"""
        |select
        |       case when date_part('year', e.date) = date_part('year', current_date) then '$currentKey' else '$previousKey' end as name,
        |       date_part('year', e.date) as year,
        |       count(distinct att.client_id) as num_clients,
        |       count(*) as total_visits,
        |       count(distinct e.id) as num_events,
        |       count(case c.gender when 'male' then 1 else null end) as count_male,
        |       count(case c.gender when 'female' then 1 else null end) as count_female
        |from attendance att
        |inner join client c on att.client_id = c.id
        |inner join event e on att.event_id = e.id
        |inner join service s on e.service_id = s.id
        |where date_part('year', e.date) between date_part('year', current_date) - 1 and date_part('year', current_date)
        |group by date_part('year', e.date)
        |
        |""".stripMargin
    )

    val rs = ps.executeQuery()

    val rsConverter = (rs: ResultSet) => {
      val year = rs.getString("year").toInt
      ServiceStats(
        rs.getString("name"),
        year,
        1,
        1,
        rs.getInt("num_clients"),
        rs.getInt("total_visits"),
        rs.getInt("num_events"),
        rs.getInt("count_male"),
        rs.getInt("count_female")
      )
    }

    val seq = createSeq(rs, rsConverter).toList
    val projection = seq.find(r => r.serviceName == currentKey).map { s =>
      val factor = getFactor()
      s.copy(serviceName = projectedKey,
        numClients = Math.round(s.numClients * factor).toInt,
        totalVisits = Math.round(s.totalVisits * factor).toInt,
        numEvents = Math.round(s.numEvents * factor).toInt,
        numMale = Math.round(s.numMale * factor).toInt,
        numFemale = Math.round(s.numFemale * factor).toInt
      )
    }

    (projection :: seq.map(r => Option(r))).flatten
  }

  private def getFactor(): Double =
    LocalDate.now()
      .withDayOfYear(1)
      .plusYears(1)
      .minusDays(1).getDayOfYear.toDouble /
      LocalDate.now().getDayOfYear.toDouble

  private def getDailyStats: List[ServiceStats] = {
    val ps = conn.prepareStatement(
      """
        |select
        |       s.name,
        |       date_part('year', e.date) as year,
        |       date_part('month', e.date) as month,
        |       date_part('day', e.date) as day,
        |       count(distinct att.client_id) as num_clients,
        |       count(*) as total_visits,
        |       count(distinct e.id) as num_events,
        |       count(case c.gender when 'male' then 1 else null end) as count_male,
        |       count(case c.gender when 'female' then 1 else null end) as count_female
        |from attendance att
        |inner join client c on att.client_id = c.id
        |inner join event e on att.event_id = e.id
        |inner join service s on e.service_id = s.id
        |group by date_part('year', e.date),
        |         date_part('month', e.date),
        |         date_part('day', e.date) ,
        |         s.name
        |
        |""".stripMargin
    )

    val rs = ps.executeQuery()

    val rsConverter = (rs: ResultSet) => {
      val year = rs.getString("year").toInt
      val month = rs.getString("month").toInt
      val day = rs.getString("day").toInt
      ServiceStats(
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

  private def getMonthlyStats: List[ServiceStats] = {
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
        |       count(distinct e.id) as num_events,
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
      ServiceStats(
        rs.getString("name"),
        year,
        month,
        day = 1,
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
