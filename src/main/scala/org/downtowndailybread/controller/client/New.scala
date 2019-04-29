package org.downtowndailybread.controller.client

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.ApiMain.secret
import org.downtowndailybread.auth.Authentication
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.ClientAttribute
import org.downtowndailybread.request.{ClientRequest, DatabaseSource}
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
