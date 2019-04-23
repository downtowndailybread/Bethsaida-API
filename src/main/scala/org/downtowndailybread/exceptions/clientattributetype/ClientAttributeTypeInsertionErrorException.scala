package org.downtowndailybread.exceptions.clientattributetype

import org.downtowndailybread.exceptions.DDBException

class ClientAttributeTypeInsertionErrorException(exception: Exception)
  extends DDBException(s"could not insert client attribute type: ${exception.getMessage}") {

  def this(urlId: String, objId: String) {
    this(new Exception(s"attribName of $urlId in uri does not match attribName in json of $objId"))
  }
}
