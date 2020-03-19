package org.downtowndailybread.bethsaida.exception.auth

import org.downtowndailybread.bethsaida.exception.DDBException

class PasswordDoesNotMatchException extends DDBException("Your password is incorrect.")