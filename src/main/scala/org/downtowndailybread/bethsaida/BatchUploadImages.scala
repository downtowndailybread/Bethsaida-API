package org.downtowndailybread.bethsaida

import java.io.File
import java.util.UUID

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import org.downtowndailybread.bethsaida.providers.{DatabaseConnectionProvider, S3Provider, SettingsProvider}


/*


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

 */
object BatchUploadImages {

  def main(args: Array[String]): Unit = {
    val s = new Settings(args)

    val hc = new DatabaseConnectionProvider with SettingsProvider with S3Provider {
      val settings = s
      def writeToS3l(bytes: Array[Byte], tag: String): Unit = writeToS3(bytes, tag)
    }

    val dir = new File("db/Picture Taken/")
    val a =List(dir.listFiles(_.isFile)).flatten

    a.foreach {
      f =>
        val id = f.getName.replace(".jpg", "")
        val tag = UUID.randomUUID()
        val image = try {
          ImmutableImage.loader().fromFile(f)
        } catch {
          case e: Exception => throw e
        }

        val scaled250 = image.scaleToWidth(250).bytes(PngWriter.MaxCompression)
        val scaled400 = image.scaleToWidth(400).bytes(PngWriter.MaxCompression)
        val full = image.bytes(PngWriter.MaxCompression)


        hc.writeToS3l(scaled250, s"${tag.toString}_250.png")
        hc.writeToS3l(scaled400, s"${tag.toString}_400.png")
        hc.writeToS3l(full, s"${tag.toString}.png")

        val sql =
          s"""
             |update client
             |set client_photo = cast(? as uuid)
             |where id = cast(? as uuid)
             |""".stripMargin

        hc.runSql(c => {
          val ps = c.prepareStatement(sql)
          ps.setString(1, tag.toString)
          ps.setString(2, id)
          ps.executeUpdate()
        })
        println("uploaded image")
        println(s"user: $id maps to tag: ${tag.toString}")
    }


  }
}
