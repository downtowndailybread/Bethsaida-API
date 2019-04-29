package org.downtowndailybread.bethsaida.model

import java.util.UUID
;

case class InternalUser(
                         id: UUID,
                         email: String,
                         name: String,
                         salt: String,
                         hash: String,
                         confirmed: Boolean,
                         resetToken: Option[UUID],
                         userLock: Boolean,
                         adminLock: Boolean
                       )

object AnonymousUser extends InternalUser(
  UUID.fromString("00000000-0000-0000-0000-000000000000"),
  "", "", "", "", true, None, false, false)