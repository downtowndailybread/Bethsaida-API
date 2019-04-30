package org.downtowndailybread.exception.user

import org.downtowndailybread.exception.DDBException

class EmailAlreadyExistsException(val email: String) extends DDBException(s"user with email $email already exists")
