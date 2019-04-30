package org.downtowndailybread.exception.user

import org.downtowndailybread.exception.NotFoundException

class UserNotFoundException extends NotFoundException("Could not find requested user")