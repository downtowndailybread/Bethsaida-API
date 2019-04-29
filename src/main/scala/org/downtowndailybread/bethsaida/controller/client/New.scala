package org.downtowndailybread.bethsaida.controller.client

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.ApiMain.secret
import org.downtowndailybread.bethsaida.auth.Authentication
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.ClientAttribute
import org.downtowndailybread.bethsaida.request.{ClientRequest, DatabaseSource}
import spray.json.{JsObject, JsString}

trait New {
  this: JsonSupport =>

  val client_newRoute =
    path("new") {
      post {
        authenticateOAuth2("ddb-api", Authentication.authenticate) {
          user =>
            entity(as[Seq[ClientAttribute]]) {
              attribs =>
                val id = DatabaseSource.runSql(c => new ClientRequest(c).insertClient(attribs))
                complete((StatusCodes.Created, JsObject(("id", JsString(id.toString)))))
            }
        }
      }
    }

}
