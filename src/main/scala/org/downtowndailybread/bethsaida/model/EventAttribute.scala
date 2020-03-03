package org.downtowndailybread.bethsaida.model

import java.time.LocalDate
import java.util.UUID

case class EventAttribute (
                  serviceId: UUID,
                  capacity: Int,
                  date: LocalDate,
                  createUserId: Option[UUID]
                )