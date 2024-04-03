package org.downtowndailybread.bethsaida.model

import java.time.LocalDateTime
import java.util.UUID

case class MealDetails(
                      id: UUID,
  date: LocalDateTime,
  breakfast: Int,
  lunch: Int

)
