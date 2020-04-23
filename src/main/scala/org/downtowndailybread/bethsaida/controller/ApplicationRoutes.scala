package org.downtowndailybread.bethsaida.controller

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.attendance.AttendanceRoutes
import org.downtowndailybread.bethsaida.controller.authentication.AuthenticationRoutes
import org.downtowndailybread.bethsaida.controller.client.ClientRoutes
import org.downtowndailybread.bethsaida.controller.event.EventRoutes
import org.downtowndailybread.bethsaida.controller.locker.LockerRoutes
import org.downtowndailybread.bethsaida.controller.mail.MailRoutes
import org.downtowndailybread.bethsaida.controller.note.NoteRoutes
import org.downtowndailybread.bethsaida.controller.service.ServiceRoutes
import org.downtowndailybread.bethsaida.controller.stats.StatsRoutes
import org.downtowndailybread.bethsaida.controller.user.UserRoutes
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers._


trait ApplicationRoutes
  extends AuthenticationRoutes
    with ClientRoutes
    with ServiceRoutes
    with EventRoutes
    with UserRoutes
    with AttendanceRoutes
    with NoteRoutes
    with StatsRoutes
    with LockerRoutes
    with MailRoutes
    with DatabaseConnectionProvider {

  this: AuthenticationProvider with SettingsProvider with JsonSupport with MaterializerProvider with S3Provider =>

  val allRoutes = ignoreTrailingSlash {
    allAuthenticationRoutes ~
      allUserRoutes ~
      allClientRoutes ~
      allServiceRoutes ~
      allEventRoutes ~
      allAttendanceRoutes ~
      allStatsRoutes ~
      allLockerRoutes ~
      allNoteRoutes ~
      allMailRoutes
  }
}
