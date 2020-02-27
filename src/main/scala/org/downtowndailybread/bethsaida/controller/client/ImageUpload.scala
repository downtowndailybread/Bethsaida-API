package org.downtowndailybread.bethsaida.controller.client

import java.io.{BufferedInputStream, BufferedWriter, CharArrayWriter, InputStream, OutputStream, OutputStreamWriter}
import java.nio.ByteBuffer
import java.nio.file.Paths
import java.util.UUID

import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.{FileIO, Framing, Keep, Sink, Source}
import akka.util.ByteString
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, MaterializerProvider, SettingsProvider}
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{ObjectCannedACL, PutObjectRequest}
import spray.json.{JsObject, JsString}

import collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

trait ImageUpload extends ControllerBase {
  this: AuthenticationProvider with JsonSupport with SettingsProvider with MaterializerProvider with MaterializerProvider =>


  implicit lazy val ec = actorMaterializer.executionContext

  private val s3 = S3Client
    .builder()
    .region(Region.US_EAST_2)
    .build()

  // Thanks Vladimir Matveev!
  //https://stackoverflow.com/questions/37430141/file-upload-using-akka-http

  val client_imageuploadroute = path("imageupload") {
    post {
      fileUpload("fileUpload") {
        case (fileInfo, fileStream) if fileInfo.getContentType.mediaType.isImage =>
          val filename = s"${UUID.randomUUID().toString}.${fileInfo.contentType.mediaType.subType}"
          uploadImageToS3(fileInfo, fileStream, filename)



        //            val sink =
        //              FileIO.toPath(Paths.get("/tmp") resolve filename)
        //            val writeResult = fileStream.runWith(sink)
        //            onSuccess(writeResult) { result =>
        //              result.status match {
        //                case Success(_) => complete(s"http://localhost:9001/$filename")
        //                case Failure(e) => throw e
        //              }
        //            }
      }
    }
  }

  private def uploadImageToS3(fileInfo: FileInfo, fileStream: Source[ByteString, Any], imageId: String): server.Route = {

    val sink = Sink.seq[ByteString]
    val writeResult = fileStream.runWith(sink).map(_.flatten)
    onSuccess(writeResult) { result =>
      s3.putObject(
        PutObjectRequest
          .builder()
          .bucket(settings.awsBucket)
          .key(imageId)
          .contentType(fileInfo.contentType.mediaType.toString())
          .acl(ObjectCannedACL.PUBLIC_READ)
          .build(),
        RequestBody.fromBytes(result.toArray)
      )
      complete(JsObject(Map("image" -> JsString(imageId))))
    }
  }

  private def saveImageToTemp(fs: Source[ByteString, Any]): Unit = {

  }
}

/*

 */