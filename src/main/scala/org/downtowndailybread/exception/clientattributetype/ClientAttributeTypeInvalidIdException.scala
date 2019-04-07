package org.downtowndailybread.exception.clientattributetype

import org.downtowndailybread.exception.DDBException

class ClientAttributeTypeInvalidIdException(id: String) extends DDBException(s"id $id contains an invalid character.")