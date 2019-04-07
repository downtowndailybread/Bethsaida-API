package org.downtowndailybread.model.exceptions

class NoSuchClientException(id: Long) extends NoSuchIdException("client", id)