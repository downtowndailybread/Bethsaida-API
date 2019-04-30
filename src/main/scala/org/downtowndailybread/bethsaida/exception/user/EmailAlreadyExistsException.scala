package org.downtowndailybread.bethsaida.exception.user

import org.downtowndailybread.bethsaida.exception.DDBException

class EmailAlreadyExistsException(val email: String) extends DDBException(s"user with email $email already exists")
