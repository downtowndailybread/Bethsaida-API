package org.downtowndailybread.exceptions.client

import org.downtowndailybread.exceptions.DDBException

class ClientAttribTypeInsertionException(exception: Exception)
  extends DDBException(s"could not insert client attribute type: ${exception.getMessage}")
