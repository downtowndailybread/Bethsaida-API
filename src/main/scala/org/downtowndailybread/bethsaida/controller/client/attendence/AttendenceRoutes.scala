//package org.downtowndailybread.bethsaida.controller.client.attendence
//
//
//import akka.http.scaladsl.server.Directives._
//import org.downtowndailybread.bethsaida.json.JsonSupport
//import spray.json.JsString
//
//trait AttendenceRoutes {
//  this: JsonSupport =>
//
//  val attendenceRoutes = {
//    path(Segment / "events") {
//      clientId =>
//        get {
//          complete(allAttendence.filter(_.client.id == clientId).map(_.event))
//          //          complete(JsString(clientId))
//        }
//    }
//  }
//
//}
