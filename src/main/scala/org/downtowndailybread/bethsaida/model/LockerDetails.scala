package org.downtowndailybread.bethsaida.model

import java.time.LocalDateTime
import java.util.UUID

case class LockerDetails(
                 lockerNumber: String,
                 clientId: UUID,
                 startDate: LocalDateTime,
                 endDate: Option[LocalDateTime],
                 expectedEndDate: LocalDateTime,
                 inputUser: UUID
                 )
