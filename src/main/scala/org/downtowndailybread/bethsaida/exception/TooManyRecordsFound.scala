package org.downtowndailybread.bethsaida.exception

abstract class TooManyRecordsFound(datatypeName: String, id: Any)
  extends NotFoundException(s"$datatypeName with ID $id contains too many records")