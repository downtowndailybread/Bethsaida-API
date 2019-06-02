package org.downtowndailybread.bethsaida.model

import spray.json.JsValue

case class ClientAttribute(
                            attributeName: String,
                            attributeValue: JsValue
                          )
