package org.downtowndailybread.bethsaida

import java.io.File
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import org.downtowndailybread.bethsaida.providers.{DatabaseConnectionProvider, S3Provider, SettingsProvider}


object BatchUploadImages {

  def main(args: Array[String]): Unit = {
    val s = new Settings(args)

    val hc = new DatabaseConnectionProvider with SettingsProvider with S3Provider {
      val settings = s
      def writeToS3l(bytes: Array[Byte], tag: String): Unit = writeToS3(bytes, tag)
    }

    val dirMap = Map(
      "db/Picture Taken/" -> "client_photo",
      "db/Photo ID/" -> "photo_id"
    )



    dirMap.foreach {
      case (directory, field) =>
        val dir = new File(directory)
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
                 |set $field = cast(? as uuid)
                 |where id = cast(? as uuid)
                 |""".stripMargin

            hc.runSql(c => {
              val ps = c.prepareStatement(sql)
              ps.setString(1, tag.toString)
              ps.setString(2, id)
              ps.executeUpdate()
            })


            hc.runSql(c => {
              val ps = c.prepareStatement(
                s"""
                   |insert into image (id, created_by, created_time)
                   |values (cast(? as uuid), cast(? as uuid), ?)
                   |""".stripMargin)
              ps.setString(1, tag.toString)
              ps.setString(2, "95a65c9a-c216-43f0-9f3f-d6573d64c873")
              ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now))
            })

            println("uploaded image")
            println(s"user: $id maps to tag: ${tag.toString}")
        }
    }
  }
}
