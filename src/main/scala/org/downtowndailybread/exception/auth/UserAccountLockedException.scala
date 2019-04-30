package org.downtowndailybread.exception.auth

import org.downtowndailybread.exception.DDBException

class UserAccountLockedException extends DDBException("user account has been locked. Please reset password")