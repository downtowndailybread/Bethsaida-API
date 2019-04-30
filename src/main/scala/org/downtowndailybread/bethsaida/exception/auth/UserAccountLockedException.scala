package org.downtowndailybread.bethsaida.exception.auth

import org.downtowndailybread.bethsaida.exception.DDBException

class UserAccountLockedException extends DDBException("user account has been locked. Please reset password")