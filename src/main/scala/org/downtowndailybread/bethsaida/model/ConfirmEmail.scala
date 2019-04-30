package org.downtowndailybread.bethsaida.model

import java.util.UUID

case class ConfirmEmail(
                         email: String,
                         token: UUID
                  )