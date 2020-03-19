package org.downtowndailybread.bethsaida.providers

import java.util.UUID

import collection.JavaConverters._
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{Delete, DeleteObjectRequest, DeleteObjectsRequest, ListObjectsRequest, ListObjectsResponse, ObjectCannedACL, ObjectIdentifier, PutObjectRequest, S3Object}

trait S3Provider {

  this: SettingsProvider =>

  private val s3 = S3Client
    .builder()
    .region(Region.US_EAST_2)
    .build()

  protected def writeToS3(bytes: Array[Byte], tag: String): Unit = {
    s3.putObject(
      PutObjectRequest
        .builder()
        .bucket(settings.awsBucket)
        .key(tag)
        .contentType("image/png")
        .acl(ObjectCannedACL.PUBLIC_READ)
        .build(),
      RequestBody.fromBytes(bytes)
    )
  }

  protected def getListOfS3Items(): ListObjectsResponse = {
    s3.listObjects(ListObjectsRequest.builder()
        .bucket(settings.awsBucket)
        .build()
    )
  }

  protected def deleteSingleTagFromS3(tag: UUID): Unit = {
    val allItems = getListOfS3Items().contents().asScala.toList.groupBy(o => UUID.fromString(o.key().take(36)))
    allItems.get(tag).foreach(deleteFromS3)
  }

  protected def deleteFromS3(objs: List[S3Object]): Unit = {
    if(objs.nonEmpty) {
      val delete =
        Delete
          .builder()
          .objects(objs.map(obj => ObjectIdentifier.builder().key(obj.key()).build()).asJavaCollection)
          .build()

      s3.deleteObjects(
        DeleteObjectsRequest.builder()
          .bucket(settings.awsBucket)
          .delete(delete)
          .build()
      )
    }
  }
}
