package org.downtowndailybread.bethsaida.exception.user

import org.downtowndailybread.bethsaida.exception.auth.UserNotAuthorizedException

class UserNotFoundException extends UserNotAuthorizedException("Could not find requested user")