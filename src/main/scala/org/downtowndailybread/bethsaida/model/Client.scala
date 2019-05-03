package org.downtowndailybread.bethsaida.model

import java.util.UUID

case class Client(
                   id: UUID,
                   attributes: Seq[ClientAttribute]
                 )
