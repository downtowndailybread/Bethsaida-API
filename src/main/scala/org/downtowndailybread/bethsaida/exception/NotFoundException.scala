package org.downtowndailybread.bethsaida.exception

abstract class NotFoundException(val message: String) extends DDBException(message)