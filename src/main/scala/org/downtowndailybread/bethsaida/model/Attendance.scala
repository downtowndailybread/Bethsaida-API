package org.downtowndailybread.bethsaida.model

import java.util.UUID

case class Attendance(
                          id: UUID,
                          eventId: UUID,
                          clientId: UUID,
                          attribute: AttendanceAttribute
                          )
