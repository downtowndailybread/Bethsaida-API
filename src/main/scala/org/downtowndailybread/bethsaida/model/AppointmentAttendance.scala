package org.downtowndailybread.bethsaida.model

import java.util.UUID

case class AppointmentAttendance(
                                id: UUID,
                                attribute: AppointmentAttendanceAttribute
                                )