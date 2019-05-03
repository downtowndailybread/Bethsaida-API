package org.downtowndailybread.bethsaida.model

import java.util.UUID

case class Service(
                    id: UUID,
                    attribute: ServiceAttribute,
                    schedules: Seq[Schedule])
