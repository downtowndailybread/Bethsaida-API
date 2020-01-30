package org.downtowndailybread.bethsaida.model

import java.util.UUID

case class Service(
                    id: UUID,
                    attributes: ServiceAttributes,
                    schedules: Seq[Schedule])
