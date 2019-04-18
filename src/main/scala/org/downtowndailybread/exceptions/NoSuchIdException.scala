package org.downtowndailybread.exceptions

import java.util.UUID

abstract class NoSuchIdException(datatypeName: String, id: Any)
                       extends DDBException(s"$datatypeName with ID $id not found")
