package org.downtowndailybread.model

case class ClientAttribute(
                            attributeType: ClientAttributeType,
                            attributeValue: String,
                            metadata: Metadata
                          ) extends ModelBase



