package org.downtowndailybread.controller.client

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.json.JsonSupport
import org.downtowndailybread.model.{Client, ClientAttribute, Success}
import org.downtowndailybread.request.{ClientRequest, DatabaseSource}

trait ClientRoutes
  extends AttendenceRoutes
    with ClientAttributeRoutes {
  this: JsonSupport =>


  val clientRoutes = {
    pathPrefix("client") {
      path("new") {
        post {
          entity(as[Seq[ClientAttribute]]) {
            attribs => complete {
              val id = DatabaseSource.runSql(c => new ClientRequest(c).insertClient(attribs))
              DatabaseSource.runSql(c => new ClientRequest(c).getClientById(id))
            }
          }
        }
      } ~
        pathPrefix(Segment) {
          idStr =>
            val id = UUID.fromString(idStr)
            pathEnd {
              get(complete(DatabaseSource.runSql(c => new ClientRequest(c).getClientById(id))))
            } ~
              path("update") {
                post {
                  entity(as[Client]) {
                    client => {
                      val id = DatabaseSource.runSql(c => new ClientRequest(c).updateClient(client))
                      complete(DatabaseSource.runSql(c => new ClientRequest(c).getClientById(id)))
                    }
                  }
                }
              } ~
              path("delete") {
                post {
                  DatabaseSource.runSql(c => new ClientRequest(c).updateClient(Client(id, Seq())))
                  complete(Success(s"client id $id successfully deleted"))
                }
              }
        } ~
      pathEnd {
        complete(DatabaseSource.runSql(c => new ClientRequest(c).getAllClientInfo()))
      }
    }
  }


}
