package org.downtowndailybread.bethsaida.exception.attendance

import org.downtowndailybread.bethsaida.exception.DDBException

class BannedUserProhibitedException extends DDBException("Client is prohibited from attending this event due to being banned from DDB services.")