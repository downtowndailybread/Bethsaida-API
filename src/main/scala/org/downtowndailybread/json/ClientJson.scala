package org.downtowndailybread.json

import java.util.UUID

import org.downtowndailybread.model.{Client, ClientAttribute, ClientAttributeType}
import org.downtowndailybread.request.{ClientAttributeTypeRequest, DatabaseSource}
import spray.json._
import DefaultJsonProtocol._

trait ClientJson {

  implicit val clientAttributeTypeToJson = new RootJsonWriter[ClientAttributeType] {
    override def write(clientAttribType: ClientAttributeType): JsValue = {
      JsObject(
        ("attribName", JsString(clientAttribType.name)),
        ("attribDataType", JsString(clientAttribType.dataType)),
        ("attribRequired", JsBoolean(clientAttribType.required)),
        ("ordering", JsNumber(clientAttribType.ordering))
      )
    }
  }

  implicit val jsonToClientAttributeType = new RootJsonReader[ClientAttributeType] {
    override def read(json: JsValue): ClientAttributeType = {
      json match {
        case JsObject(fields) =>
          ClientAttributeType(
            fields("attribName") match { case JsString(value) => value},
            fields("attribDataType") match { case JsString(value) => value},
            fields("attribRequired") match { case JsBoolean(value) => value},
            fields("ordering") match { case JsNumber(value) => value.toInt
            }
          )
      }
    }
  }


  implicit val clientAttributeToJson = new RootJsonWriter[ClientAttribute] {
    override def write(clientAttrib: ClientAttribute): JsValue = {
      JsObject(
        ("attribType", clientAttributeTypeToJson.write(clientAttrib.attributeType)),
        ("attribValue", clientAttrib.attributeValue)
      )
    }
  }

  implicit val jsonToClientAttribute = new RootJsonReader[ClientAttribute] {
    override def read(json: JsValue): ClientAttribute = {
      val allTypes = DatabaseSource.runSql(c => new ClientAttributeTypeRequest(c).getClientAttributeTypes())
        .map(att => (att._2.name, att._2)).toMap
      json match {
        case JsObject(obj) =>
          ClientAttribute(
            allTypes(obj("type") match { case JsString(s) => s }),
            obj("value")
          )
      }
    }
  }

  implicit val clientAttributeTypeSeqToJson = new RootJsonWriter[Seq[ClientAttributeType]] {
    override def write(obj: Seq[ClientAttributeType]): JsValue = {
      JsArray(obj.map(cat => clientAttributeTypeToJson.write(cat)).toVector)
    }
  }

  implicit val jsonToClientAttributeTypeSeq = new RootJsonReader[Seq[ClientAttributeType]] {
    override def read(json: JsValue): Seq[ClientAttributeType] = {
      json match {
        case JsArray(r) => r.map(jsonToClientAttributeType.read)
      }
    }
  }


  implicit val clientAttributeSeqToJson = new RootJsonWriter[Seq[ClientAttribute]] {
    override def write(obj: Seq[ClientAttribute]): JsValue = {
      JsArray(obj.map(ca => clientAttributeToJson.write(ca)).toVector)
    }
  }




  implicit val jsonArrayToClientAttributeArray = new RootJsonReader[Seq[ClientAttribute]] {
    override def read(json: JsValue): Seq[ClientAttribute] = {
      json match {
        case JsArray(elements) =>
          elements.map {
            case element =>
              element.convertTo[ClientAttribute]
          }
      }
    }
  }

  implicit val clientToJson = new RootJsonWriter[Client] {
    override def write(client: Client): JsValue = {
      JsObject(
        ("id", JsString(client.id.toString)),
        ("attributes", JsObject(
          client.attributes.map(attrib => (attrib.attributeType.name, clientAttributeToJson.write(attrib))).toMap
        )))
    }
  }


  implicit val jsonToClient = new RootJsonReader[Client] {
    override def read(json: JsValue): Client = {
      val allClientAttributeTypes =
        DatabaseSource.runSql(c => new ClientAttributeTypeRequest(c).getClientAttributeTypes())
          .map(r => (r._2.name, r._2)).toMap
      json match {
        case JsObject(c) =>
          Client(
            c("id") match { case JsString(s) => UUID.fromString(s)},
            (c("attributes") match {
              case JsObject(attribs) => attribs.map{ case (k, v) => ClientAttribute(allClientAttributeTypes(k), v)}
            }).toSeq

          )
      }
    }
  }

  implicit val clientSeqToJson = new RootJsonWriter[Seq[Client]] {
    override def write(obj: Seq[Client]): JsValue = JsArray(obj.map(clientToJson.write).toVector)
  }
}
