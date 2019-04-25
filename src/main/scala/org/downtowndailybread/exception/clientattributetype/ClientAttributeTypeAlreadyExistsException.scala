package org.downtowndailybread.exception.clientattributetype

import org.downtowndailybread.exception.DDBException

class ClientAttributeTypeAlreadyExistsException(val name: String)
  extends DDBException(s"Client attribute type of id $name already exists")
