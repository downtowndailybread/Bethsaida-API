package org.downtowndailybread.exceptions.clientattributetype

import org.downtowndailybread.exceptions.DDBException

class ClientAttributeTypeNotFoundException(val name: String)
  extends DDBException(s"Client attribute type of id $name not found")
