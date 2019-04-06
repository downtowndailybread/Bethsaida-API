package org.downtowndailybread.model

case class Client(
                 attributes: Seq[ClientAttribute],
                 metadata: Metadata
                 ) extends ModelBase
