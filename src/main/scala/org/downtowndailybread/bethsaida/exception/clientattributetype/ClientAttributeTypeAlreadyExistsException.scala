package org.downtowndailybread.bethsaida.exception.clientattributetype

import org.downtowndailybread.bethsaida.exception.DDBException

class ClientAttributeTypeAlreadyExistsException(val name: String)
  extends DDBException(s"Client attribute type of id $name already exists")
