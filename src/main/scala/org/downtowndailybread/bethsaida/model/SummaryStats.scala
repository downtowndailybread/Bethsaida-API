package org.downtowndailybread.bethsaida.model

case class SummaryStats(
                       numClients: Int,
                       numAttendanceSheets: Int,
                       numUniqueVisits: Int,
                       monthlyStats: List[ServiceMonthlyStats],
                       dailyStats: List[ServiceDailyStats]
                       )
