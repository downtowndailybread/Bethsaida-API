package org.downtowndailybread.exception.auth

import org.downtowndailybread.exception.DDBException

class PasswordDoesNotMatchException extends DDBException("passwords do not match")