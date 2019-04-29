package org.downtowndailybread.model

import java.util.UUID
;

case class InternalUser(
               id: UUID,
               email: String,
               name: String,
               salt: String,
               hash: String,
               confirmed: Boolean,
               resetToken: UUID,
               userLock: Boolean,
               adminLock: Boolean
        )
