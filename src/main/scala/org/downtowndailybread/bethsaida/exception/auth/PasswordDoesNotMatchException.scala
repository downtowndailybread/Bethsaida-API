package org.downtowndailybread.bethsaida.exception.auth

import org.downtowndailybread.bethsaida.exception.DDBException

class PasswordDoesNotMatchException extends DDBException("passwords do not match")