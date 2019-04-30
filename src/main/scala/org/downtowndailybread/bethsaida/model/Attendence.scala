package org.downtowndailybread.bethsaida.model

case class Attendence(
                     client: Client,
                     event: Event,
                     metadata: Metadata
                     ) extends ModelBase
