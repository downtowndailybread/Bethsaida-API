package org.downtowndailybread.model

import java.time.LocalDateTime

case class Event(
                  service: Service,
                  eventTime: LocalDateTime,
                  metadata: Metadata) extends ModelBase