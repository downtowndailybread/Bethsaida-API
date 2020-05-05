package org.downtowndailybread.bethsaida.model

case class SummaryStats(
                       numClients: Int,
                       numAttendanceSheets: Int,
                       numUniqueVisits: Int,
                       monthlyStats: Map[Int, Map[Int, Map[String, ServiceMonthlyStats]]]
                       )
