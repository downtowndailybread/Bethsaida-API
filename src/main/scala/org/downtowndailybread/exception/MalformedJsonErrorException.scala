package org.downtowndailybread.exception

class MalformedJsonErrorException(val message: String) extends DDBException(message)