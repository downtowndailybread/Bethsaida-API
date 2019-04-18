package org.downtowndailybread.exceptions

import java.util.UUID

class NoSuchServiceException(id: UUID) extends NoSuchIdException("service", id)