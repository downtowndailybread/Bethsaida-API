package org.downtowndailybread.model

import org.downtowndailybread.exception.clientattributetype.ClientAttributeTypeInvalidIdException

case class ClientAttributeType(
                                id: String,
                                clientAttributeTypeAttribute: ClientAttributeTypeAttribute
                              ) {
  if (!id.matches("^[A-Za-z0-9_-]*$")) {
    throw new ClientAttributeTypeInvalidIdException(id)
  }
}
