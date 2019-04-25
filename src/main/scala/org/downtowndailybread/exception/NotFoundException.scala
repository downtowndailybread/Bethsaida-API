package org.downtowndailybread.exception

abstract class NotFoundException(val message: String) extends DDBException(message)