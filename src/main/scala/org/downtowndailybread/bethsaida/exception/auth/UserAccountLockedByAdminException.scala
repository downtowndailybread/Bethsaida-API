package org.downtowndailybread.bethsaida.exception.auth

import org.downtowndailybread.bethsaida.exception.DDBException

class UserAccountLockedByAdminException extends DDBException("user account has been locked by the administrator")