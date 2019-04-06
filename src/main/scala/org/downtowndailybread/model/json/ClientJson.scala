package org.downtowndailybread.model.json

import org.downtowndailybread.model.{Client, ClientAttribute, ClientAttributeTypes, Name}
import spray.json.{JsArray, JsBoolean, JsNumber, JsObject, JsString, JsValue, RootJsonWriter}

trait ClientJson {
  implicit val clientAttributeFormat = new RootJsonWriter[ClientAttribute] {
    override def write(clientAttrib: ClientAttribute): JsValue = {
      JsObject(
        ("attribName", JsString(clientAttrib.attributeType.name)),
        ("attribType", JsString(clientAttrib.attributeType.dataType)),
        ("attribValue", JsString(clientAttrib.attributeValue))
      )
    }
  }

  implicit val clientFormat = new RootJsonWriter[Client] {
    override def write(client: Client): JsValue = {
      JsObject(
        ("id", JsNumber(client.metadata.id)),
        ("name", JsString(
          client.attributes.find(_.attributeType == Name).map(_.attributeValue).get)),
        ("attributes", JsObject(
          client.attributes.map(attrib => (attrib.attributeType.name, clientAttributeFormat.write(attrib))).toMap
        )),
        ("onboardingComplete", JsBoolean(
          ClientAttributeTypes.attributes.filter(_.required).forall(
            client.attributes.map(_.attributeType).contains
          )
        )))
    }
  }

  implicit val clientSeqFormat = new RootJsonWriter[Seq[Client]] {
    override def write(obj: Seq[Client]): JsValue = JsArray(obj.map(clientFormat.write).toVector)
  }

}
