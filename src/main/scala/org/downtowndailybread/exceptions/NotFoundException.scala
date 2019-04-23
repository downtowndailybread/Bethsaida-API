package org.downtowndailybread.exceptions

abstract class NotFoundException(val message: String) extends DDBException(message)