package org.downtowndailybread.bethsaida.json

import java.util.UUID

import org.downtowndailybread.bethsaida.exception.MalformedJsonErrorException
import spray.json._
import org.downtowndailybread.bethsaida.service.UUIDProvider

trait BaseSupport extends UUIDProvider {
  implicit val uuidFormat = new RootJsonFormat[UUID] {
    override def read(json: JsValue): UUID = json match {
      case JsString(s) => parseUUID(s)
      case s => throw new MalformedJsonErrorException(s"could not parse UUID: $s")
    }

    override def write(obj: UUID): JsValue = JsString(obj.toString)
  }

  implicit def seqFormat[T : JsonFormat](implicit format: JsonFormat[T]): RootJsonFormat[Seq[T]] =
    new RootJsonFormat[Seq[T]] {
    override def read(json: JsValue): Seq[T] = json match {
      case JsArray(arr) => arr.map(arrVal => format.read(arrVal)).toList
      case s => throw new MalformedJsonErrorException(s"could not seq: $s")
    }

    override def write(obj: Seq[T]): JsValue = JsArray(obj.map(format.write).toVector)
  }

}
