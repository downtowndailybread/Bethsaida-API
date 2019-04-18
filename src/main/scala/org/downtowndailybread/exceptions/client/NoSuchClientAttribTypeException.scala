package org.downtowndailybread.exceptions.client

import org.downtowndailybread.exceptions.NoSuchIdException

class NoSuchClientAttribTypeException(id: Int) extends NoSuchIdException("client_attrib_type", id)