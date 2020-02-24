package org.downtowndailybread.bethsaida.controller.client

import java.nio.file.Paths
import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.FileIO
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, MaterializerProvider, SettingsProvider}

import scala.util.{Failure, Success}

trait ImageUpload extends ControllerBase {
  this: AuthenticationProvider with JsonSupport with SettingsProvider with MaterializerProvider =>

  // Thanks Vladimir Matveev!
  //https://stackoverflow.com/questions/37430141/file-upload-using-akka-http

  val client_imageuploadroute = path("imageupload") {
//    id =>
      post {
        fileUpload("fileUpload") {
          case (fileInfo, fileStream) if fileInfo.getContentType.mediaType.isImage =>
            val filename = s"${UUID.randomUUID().toString}.${fileInfo.contentType.mediaType.subType}"
            val sink =
              FileIO.toPath(Paths.get("/tmp") resolve filename)
            val writeResult = fileStream.runWith(sink)
            onSuccess(writeResult) { result =>
              result.status match {
                case Success(_) => complete(s"http://localhost:9001/$filename")
                case Failure(e) => throw e
              }
            }
        }
      }
  }
}

/*

 */