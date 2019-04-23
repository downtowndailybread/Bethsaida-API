package org.downtowndailybread.controller.client


import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.helper.AttribNameValuePair
import org.downtowndailybread.request.{ClientRequest, DatabaseSource}

class ClientNew extends JsonSupport {

  val newClientRoute =
    path("new") {
      post {
        entity(as[Seq[AttribNameValuePair]]) {
          attribs =>
            complete {
              val id = DatabaseSource.runSql(c => new ClientRequest(c).insertClient(attribs))
              DatabaseSource.runSql(c => new ClientRequest(c).getClientById(id))
            }
        }
      }
    }

}
