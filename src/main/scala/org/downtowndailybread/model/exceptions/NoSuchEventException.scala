package org.downtowndailybread.model.exceptions

class NoSuchEventException(id: Long) extends NoSuchIdException("event", id)