package org.downtowndailybread.bethsaida.controller.user

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.emailer.Emailer
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.parameters.UserParameters
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.UserRequest

trait New extends ControllerBase {
  this: AuthenticationProvider
    with JsonSupport
    with DatabaseConnectionProvider
    with SettingsProvider =>

  val user_newRoute = path("new") {
    authorize(_.admin) {
      implicit authUser =>
        post {
          entity(as[UserParameters]) {
            us =>
              futureCompleteCreated {
                val user =
                  runSql{c =>
                    val uuid = new UserRequest(settings, c).insertUser(us)
                    new UserRequest(settings, c).getRawUserFromUuid(uuid)
                  }

                Emailer.sendEmail(
                  us.loginParameters.email,
                  "Complete your Bethsaida registration",
                  "You have been registered for Bethsaida, the client database for Downtown Daily Bread.\n\nPlease click the following link to complete your signup.\n\n" +
                    s"https://${if(settings.isDev) "edge." else ""}bethsaida.downtowndailybread.org/confirm/${user.email}/${user.resetToken.getOrElse("")}",
                  settings
                )
                user.id
              }
          }
        }
    }
  }
}
