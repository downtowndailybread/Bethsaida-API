package org.downtowndailybread.exception.client

import org.downtowndailybread.exception.DDBException

class IncorrectPasswordException extends DDBException("passwords do not match")
