package org.downtowndailybread.model

import java.util.UUID

case class ClientAttributeType(
                                name: String,
                                dataType: String,
                                required: Boolean,
                                ordering: Int
                              )

case object ClientAttributeType extends CanonicalDataType("client_attribute_type")