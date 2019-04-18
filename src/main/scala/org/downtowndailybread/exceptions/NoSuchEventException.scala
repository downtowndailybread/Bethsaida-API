package org.downtowndailybread.exceptions

import java.util.UUID

class NoSuchEventException(id: UUID) extends NoSuchIdException("event", id)