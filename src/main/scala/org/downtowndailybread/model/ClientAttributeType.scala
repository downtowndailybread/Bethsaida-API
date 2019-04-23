package org.downtowndailybread.model

import java.util.UUID

case class ClientAttributeType(
                                name: String,
                                displayName: String,
                                dataType: String,
                                required: Boolean,
                                requiredForOnboarding: Boolean,
                                ordering: Int
                              )

case object ClientAttributeType extends CanonicalDataType("client_attribute_type")

case class ClientAttributeTypeInternal(
                                      id: UUID,
                                      tpe: ClientAttributeType
                                      )