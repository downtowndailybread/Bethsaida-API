package org.downtowndailybread.bethsaida.exception.clientattributetype

import org.downtowndailybread.bethsaida.exception.DDBException

class ClientAttributeTypeInvalidIdException(id: String) extends DDBException(s"id $id contains an invalid character.")