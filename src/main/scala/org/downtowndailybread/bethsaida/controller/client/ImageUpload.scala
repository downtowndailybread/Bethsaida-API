package org.downtowndailybread.bethsaida.controller.client

import java.io.{BufferedInputStream, BufferedWriter, CharArrayWriter, InputStream, OutputStream, OutputStreamWriter}
import java.nio.ByteBuffer
import java.nio.file.Paths
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
      fileUpload("fileUpload") {
        case (fileInfo, fileStream) if fileInfo.getContentType.mediaType.isImage =>
          val fileTag = UUID.randomUUID().toString
          uploadImageToS3(fileInfo, fileStream, fileTag)
      }
    }
  }

  private def uploadImageToS3(fileInfo: FileInfo, fileStream: Source[ByteString, Any], fileTag: String): server.Route = {

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

      writeToS3(scaled250, s"${fileTag}_250.png")
      writeToS3(scaled400, s"${fileTag}_400.png")
      writeToS3(full, s"${fileTag}.png")

      complete(JsObject(Map("image" -> JsString(fileTag))))
    }
  }
}

/*

 */