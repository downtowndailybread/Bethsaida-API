package org.downtowndailybread.bethsaida.model

case class SummaryStats(
                       numClients: Int,
                       numAttendanceSheets: Int,
                       numUniqueVisits: Int,
                       monthlyStats: List[ServiceStats],
                       dailyStats: List[ServiceStats],
                       yearlyStats: List[ServiceStats],
                       yearlyRaceStats: List[RaceStats],
                       covidStats: CovidStats
                       )
