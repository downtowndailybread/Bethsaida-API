package org.downtowndailybread.bethsaida.model

import java.util.UUID

case class Event(
                  id: UUID,
                  serviceId: UUID,
                  attribute: EventAttribute
                )
