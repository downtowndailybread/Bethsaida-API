package org.downtowndailybread.bethsaida.exception.service

import java.util.UUID

import org.downtowndailybread.bethsaida.exception.NoSuchIdException

class ServiceNotFoundException(id: UUID) extends NoSuchIdException("service", id)
