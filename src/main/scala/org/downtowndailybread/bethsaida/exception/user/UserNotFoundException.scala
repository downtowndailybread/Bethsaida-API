package org.downtowndailybread.bethsaida.exception.user

import org.downtowndailybread.bethsaida.exception.NotFoundException

class UserNotFoundException extends NotFoundException("Could not find requested user")