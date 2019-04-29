package org.downtowndailybread.bethsaida.exception.auth

import org.downtowndailybread.bethsaida.exception.DDBException

class InvalidTokenSignatureException extends DDBException("provided token contains invalid signature")
