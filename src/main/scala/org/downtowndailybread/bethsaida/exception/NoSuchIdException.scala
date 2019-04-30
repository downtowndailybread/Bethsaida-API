package org.downtowndailybread.bethsaida.exception

abstract class NoSuchIdException(datatypeName: String, id: Any)
                       extends NotFoundException(s"$datatypeName with ID $id not found")
