package org.downtowndailybread.model.exceptions

class NoSuchServiceException(id: Long) extends NoSuchIdException("service", id)