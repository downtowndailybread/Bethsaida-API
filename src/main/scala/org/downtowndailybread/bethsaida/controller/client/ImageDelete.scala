package org.downtowndailybread.bethsaida.controller.client

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, S3Provider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.{ClientRequest, ImageRequest}

import scala.util.Success

trait ImageDelete  extends ControllerBase {
  this: JsonSupport with AuthenticationProvider with SettingsProvider with DatabaseConnectionProvider with S3Provider =>

  val client_imagedeleteRoute = path("deleteImage" / JavaUUID) {
    photoId =>
      get {
        authorizeNotAnonymous {
          implicit authUser =>
            runSql(c => new ImageRequest(settings, c).deleteImage(Some(photoId)))
            deleteSingleTagFromS3(photoId)
            complete(Success(s"client photo $photoId successfully deleted"))
        }
      }
  }
}
