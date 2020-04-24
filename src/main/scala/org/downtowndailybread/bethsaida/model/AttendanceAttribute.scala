package org.downtowndailybread.bethsaida.model

import java.time.ZonedDateTime
import java.util.UUID

case class AttendanceAttribute(
                                eventId: UUID,
                                clientId: UUID,
                                checkInTime: ZonedDateTime,
                                userId: UUID
                              )
