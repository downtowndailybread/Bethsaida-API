package org.downtowndailybread.model.exceptions

abstract class NoSuchIdException(datatypeName: String, id: Long)
                       extends DDBException(s"$datatypeName with ID $id not found")
