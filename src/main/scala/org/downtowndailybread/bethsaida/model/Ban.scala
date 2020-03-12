package org.downtowndailybread.bethsaida.model

import java.util.UUID

case class Ban(
                id: UUID,
                clientId: UUID,
                user: InternalUser,
                attributes: BanAttribute
              )
