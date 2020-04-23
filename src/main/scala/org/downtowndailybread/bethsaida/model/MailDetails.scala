package org.downtowndailybread.bethsaida.model

import java.time.LocalDateTime
import java.util.UUID

case class MailDetails(
                 clientId: UUID,
                 startDate: LocalDateTime,
                 endDate: Option[LocalDateTime],
                 inputUser: UUID
                 )
