package org.downtowndailybread.bethsaida.providers

import java.util.UUID

trait UUIDProvider {

  def getUUID(): UUID = UUID.randomUUID()

  implicit def parseUUID(id: String): UUID = UUID.fromString(id)

  implicit def uuidToString(uuid: UUID): String = uuid.toString
}
