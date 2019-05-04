package org.downtowndailybread.bethsaida.model

import java.time.LocalDateTime

case class Event(
                  service: Service,
                  eventTime: LocalDateTime,
                  metadata: Metadata)
