package org.downtowndailybread.bethsaida.model

import java.time.LocalTime

case class Schedule(
                   rrule: String,
                   startTime: LocalTime,
                   endTime: LocalTime
                   )
