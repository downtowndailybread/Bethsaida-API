package org.downtowndailybread.bethsaida.model

import java.util.UUID

import org.downtowndailybread.bethsaida.exception.clientattributetype.ClientAttributeTypeInvalidIdException

case class ClientAttributeType(
                                id: String,
                                clientAttributeTypeAttribute: ClientAttributeTypeAttribute
                              ) {
  if (!id.matches("^[A-Za-z0-9_-]*$")) {
    throw new ClientAttributeTypeInvalidIdException(id)
  }
}
