package org.downtowndailybread.bethsaida

import org.downtowndailybread.bethsaida.model.AnonymousUser
import org.downtowndailybread.bethsaida.model.parameters.{LoginParameters, UserParameters}
import org.downtowndailybread.bethsaida.providers.{DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.UserRequest

object Bootstrap {

  def main(args: Array[String]): Unit = {
    val s = new Settings(args)

    val sqlRunner = new DatabaseConnectionProvider with SettingsProvider {
      val settings = s
    }

    val numUsers = sqlRunner.runSql(conn => new UserRequest(s, conn).getAllUsers()).size

    if(numUsers == 0) {
      sqlRunner.runSql(conn => new UserRequest(s, conn).insertUser(UserParameters(
        "Administrator",
        LoginParameters(
          s.emailFrom,
          "admin"
        ),
        Some(true)
      ))(AnonymousUser))

      val user = sqlRunner.runSql(conn => new UserRequest(s, conn).getAllUsers()).head

      sqlRunner.runSql(conn => new UserRequest(s, conn).confirmEmail(user.email, user.resetToken.get)(AnonymousUser))
    }
  }
}
