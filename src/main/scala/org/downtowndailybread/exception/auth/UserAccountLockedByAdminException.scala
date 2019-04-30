package org.downtowndailybread.exception.auth

import org.downtowndailybread.exception.DDBException

class UserAccountLockedByAdminException extends DDBException("user account has been locked by the administrator")