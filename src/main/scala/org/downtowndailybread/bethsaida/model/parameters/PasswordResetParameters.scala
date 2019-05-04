package org.downtowndailybread.bethsaida.model.parameters

import java.util.UUID

case class PasswordResetParameters(
                                    email: String,
                                    token: UUID,
                                    password: String
                                  )