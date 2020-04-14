package org.downtowndailybread.bethsaida.request

import java.sql.Connection

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.model.SummaryStats
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
      getNumberOfVisits
    )
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
