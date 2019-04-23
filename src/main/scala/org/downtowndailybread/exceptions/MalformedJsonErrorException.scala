package org.downtowndailybread.exceptions

class MalformedJsonErrorException(val message: String) extends DDBException(message)