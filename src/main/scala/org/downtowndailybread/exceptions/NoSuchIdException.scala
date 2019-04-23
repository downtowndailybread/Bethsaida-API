package org.downtowndailybread.exceptions

abstract class NoSuchIdException(datatypeName: String, id: Any)
                       extends DDBException(s"$datatypeName with ID $id not found")
