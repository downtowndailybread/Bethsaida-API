package org.downtowndailybread.bethsaida.exception.clientattributetype

import org.downtowndailybread.bethsaida.exception.NoSuchIdException

class ClientAttributeTypeNotFoundException(val name: String)
  extends NoSuchIdException(s"client attribute type", name)
