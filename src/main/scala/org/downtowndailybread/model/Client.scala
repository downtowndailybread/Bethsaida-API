package org.downtowndailybread.model

import java.util.UUID


case class Client(
                   id: UUID,
                   attributes: Seq[ClientAttribute]
                 ) extends ModelBase



