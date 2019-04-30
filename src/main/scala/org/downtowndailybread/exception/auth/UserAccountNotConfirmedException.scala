package org.downtowndailybread.exception.auth

import org.downtowndailybread.exception.DDBException

class UserAccountNotConfirmedException extends DDBException("user account has not been confirmed by end user")
