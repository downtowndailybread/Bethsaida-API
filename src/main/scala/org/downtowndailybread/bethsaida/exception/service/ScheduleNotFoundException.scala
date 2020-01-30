package org.downtowndailybread.bethsaida.exception.service

import java.util.UUID

import org.downtowndailybread.bethsaida.exception.NoSuchIdException

class ScheduleNotFoundException(id: UUID) extends NoSuchIdException("schedule", id)