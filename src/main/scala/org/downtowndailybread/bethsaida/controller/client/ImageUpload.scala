package org.downtowndailybread.bethsaida.controller.client

import java.nio.file.{Files, Path, Paths}
import java.util.UUID

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.exception.InvalidImageFormat
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, MaterializerProvider, S3Provider, SettingsProvider}
import spray.json.{JsObject, JsString}


trait ImageUpload extends ControllerBase {
  this: AuthenticationProvider
    with JsonSupport
    with SettingsProvider
    with MaterializerProvider
    with MaterializerProvider
    with S3Provider
  =>


  implicit lazy val ec = actorMaterializer.executionContext


  // Thanks Vladimir Matveev!
  //https://stackoverflow.com/questions/37430141/file-upload-using-akka-http

  val client_imageuploadRoute = path("imageupload") {
    post {
      withoutSizeLimit {
        fileUpload("fileUpload") {
          case (fileInfo, fileStream) if fileInfo.getContentType.mediaType.isImage =>
            val fileTag = UUID.randomUUID().toString

            val uploader = uploadToTarget(fileInfo, fileStream, fileTag) _

            //            uploader(tmp_writer)
            uploader(writeToS3)
        }
      }
    }
  }

  private def tmp_writer(stream: Array[Byte], s: String): Unit = {
    Files.write(Paths.get(s"./images/$s"), stream)
  }

  private def uploadToTarget(
                              fileInfo: FileInfo,
                              fileStream: Source[ByteString, Any],
                              fileTag: String)(
                              target: (Array[Byte], String) => Unit
                            ): server.Route = {

    val sink = Sink.seq[ByteString]
    val writeResult = fileStream.runWith(sink).map(_.flatten).map(_.toArray)
    onSuccess(writeResult) { result =>


      val image = try {
        ImmutableImage.loader().fromBytes(result)
      } catch {
        case e: Exception => throw new InvalidImageFormat
      }

      val scaled250 = image.scaleToWidth(250).bytes(PngWriter.MaxCompression)
      val scaled400 = image.scaleToWidth(400).bytes(PngWriter.MaxCompression)
      val full = image.bytes(PngWriter.MaxCompression)


      target(scaled250, s"${fileTag}_250.png")
      target(scaled400, s"${fileTag}_400.png")
      target(full, s"${fileTag}.png")


      complete(JsObject(Map("image" -> JsString(fileTag))))
    }
  }
}
