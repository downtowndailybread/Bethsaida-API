package org.downtowndailybread.bethsaida.model

import java.util.UUID

case class EventAttribute (
                  hours: HoursOfOperation,
                  capacity: Option[Int],
                  userCreatorId: Option[UUID],
                  scheduleCreatorId: Option[UUID]
                )