package org.downtowndailybread.model

case class Attendence(
                     client: Client,
                     event: Event,
                     metadata: Metadata
                     ) extends ModelBase
