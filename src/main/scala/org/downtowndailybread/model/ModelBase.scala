package org.downtowndailybread.model

trait ModelBase {
  val metadata: Metadata
  val id = metadata.id
}
