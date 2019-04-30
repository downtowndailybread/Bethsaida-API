package org.downtowndailybread.model

import java.util.UUID

case class ConfirmEmail(
                         email: String,
                         token: UUID
                  )