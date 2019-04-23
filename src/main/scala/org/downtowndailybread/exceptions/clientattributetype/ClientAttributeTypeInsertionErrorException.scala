package org.downtowndailybread.exceptions.clientattributetype

import org.downtowndailybread.exceptions.DDBException

class ClientAttributeTypeInsertionErrorException(exception: Exception)
  extends DDBException(s"could not insert client attribute type: ${exception.getMessage}")
