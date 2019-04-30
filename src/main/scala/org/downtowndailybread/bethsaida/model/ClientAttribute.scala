package org.downtowndailybread.bethsaida.model

import spray.json.JsValue

case class ClientAttribute(
                            attributeType: ClientAttributeType,
                            attributeValue: JsValue
                          ) extends ModelBase
