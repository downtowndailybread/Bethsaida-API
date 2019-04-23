package org.downtowndailybread.exceptions.clientattributetype

import org.downtowndailybread.exceptions.{DDBException, NoSuchIdException}

class ClientAttributeTypeNotFoundException(val name: String)
  extends NoSuchIdException(s"client attribute type", name)
