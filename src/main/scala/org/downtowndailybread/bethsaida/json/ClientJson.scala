package org.downtowndailybread.bethsaida.json

import org.downtowndailybread.bethsaida.model._
import spray.json._
import DefaultJsonProtocol._
import org.downtowndailybread.bethsaida.exception.{DDBException, MalformedJsonErrorException}
import org.downtowndailybread.bethsaida.exception.clientattributetype.ClientAttributeTypeNotFoundException
import org.downtowndailybread.bethsaida.providers.{DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.ClientAttributeTypeRequest


trait ClientJson extends BaseSupport {
  this: DatabaseConnectionProvider with SettingsProvider =>

  implicit val clientAttributeTypeAttribFormat = new RootJsonFormat[ClientAttributeTypeAttribute] {
    override def read(json: JsValue): ClientAttributeTypeAttribute = {
      json match {
        case JsObject(fields) =>
          if (fields.get("id").isDefined) {
            throw new MalformedJsonErrorException("id cannot be updated")
          }
          ClientAttributeTypeAttribute(
            fields("name").convertTo[String],
            fields("datatype").convertTo[String],
            fields("required").convertTo[Boolean],
            fields("requiredForOnboarding").convertTo[Boolean],
            fields("ordering").convertTo[Int]
          )
        case _ => throw new MalformedJsonErrorException("could not parse client attribute type attrib")
      }
    }

    override def write(obj: ClientAttributeTypeAttribute): JsValue = {
      JsObject(
        ("name", JsString(obj.displayName)),
        ("datatype", JsString(obj.dataType)),
        ("required", JsBoolean(obj.required)),
        ("requiredForOnboarding", JsBoolean(obj.requiredForOnboarding)),
        ("ordering", JsNumber(obj.ordering))
      )
    }
  }

  implicit val clientAttributeTypeFormat = new RootJsonFormat[ClientAttributeType] {
    override def read(json: JsValue): ClientAttributeType = {
//      val allAttribs = new ClientAttributeTypeRequest(settings, settings.ds.getConnection).getClientAttributeTypes()
      json match {
        case JsObject(fields) => ClientAttributeType(
          fields("id").convertTo[String],
          clientAttributeTypeAttribFormat.read(JsObject(fields - "id"))
        )
        case _ => throw new MalformedJsonErrorException("could not parse client attribute type")
      }
    }

    override def write(obj: ClientAttributeType): JsValue = {
      val oMap =
        JsObject.unapply(clientAttributeTypeAttribFormat.write(obj.clientAttributeTypeAttribute).asJsObject).get
      JsObject(oMap + (("id", JsString(obj.id))))
    }
  }

  implicit val seqClientAttributeTypeFormat = seqFormat[ClientAttributeType]

  implicit val seqClientAttributeFormat = new RootJsonFormat[Seq[ClientAttribute]] {

    override def read(json: JsValue): Seq[ClientAttribute] = {
      (json: @unchecked) match {
        case JsArray(arr) => arr.map {
          attrib =>
            (attrib: @unchecked) match {
              case JsObject(m) =>
                val lookup = m("id").convertTo[String]
                ClientAttribute(lookup, m("value"))
            }
        }
      }
    }

    override def write(objSeq: Seq[ClientAttribute]): JsValue = {
      JsArray(objSeq.map(obj =>
        JsObject(
          ("id", JsString(obj.attributeName)),
          ("value", obj.attributeValue)
        )
      ).toVector
      )
    }
  }

  implicit val clientFormat = new RootJsonFormat[Client] {
    override def read(json: JsValue): Client = {
      (json: @unchecked) match {
        case JsObject(obj) => Client(
          obj("id").convertTo[String],
          (obj("attributes"): @unchecked) match {
            case a: JsArray => seqClientAttributeFormat.read(a)
          }
        )
      }
    }

    override def write(obj: Client): JsValue = {
      JsObject(
        ("id", JsString(obj.id)),
        ("attributes", seqClientAttributeFormat.write(obj.attributes))
      )

    }
  }

  implicit val seqClientFormat = seqFormat[Client]
}

