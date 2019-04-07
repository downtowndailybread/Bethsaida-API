package org.downtowndailybread.controller.client


import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.ClientAttribute
import org.downtowndailybread.request.{ClientRequest, DatabaseSource}
import spray.json.{JsObject, JsString}

class ClientNew extends JsonSupport {

  val newClientRoute =
    path("new") {
      post {
        entity(as[Seq[ClientAttribute]]) {
          attribs =>
            val id = DatabaseSource.runSql(c => new ClientRequest(c).insertClient(attribs))
            complete((StatusCodes.Created, JsObject(("id", JsString(id.toString)))))
        }
      }
    }

}
