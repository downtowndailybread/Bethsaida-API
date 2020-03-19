package org.downtowndailybread.bethsaida.exception.client

import org.downtowndailybread.bethsaida.exception.DDBException

class DuplicateBanException extends DDBException("Duplicate ban records. Each client can have max of one record")