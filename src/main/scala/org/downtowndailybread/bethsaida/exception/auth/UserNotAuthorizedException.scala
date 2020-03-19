package org.downtowndailybread.bethsaida.exception.auth

import org.downtowndailybread.bethsaida.exception.{DDBException, UnauthorizedException}

class UserNotAuthorizedException(str: String) extends UnauthorizedException