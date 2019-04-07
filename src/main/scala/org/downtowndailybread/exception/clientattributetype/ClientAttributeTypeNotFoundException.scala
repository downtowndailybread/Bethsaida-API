package org.downtowndailybread.exception.clientattributetype

import org.downtowndailybread.exception.NoSuchIdException

class ClientAttributeTypeNotFoundException(val name: String)
  extends NoSuchIdException(s"client attribute type", name)
