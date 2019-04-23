package org.downtowndailybread.exceptions.clientattributetype

import org.downtowndailybread.exceptions.DDBException

class ClientAttributeTypeAlreadyExistsException(val name: String)
  extends DDBException(s"Client attribute type of id $name already exists")
