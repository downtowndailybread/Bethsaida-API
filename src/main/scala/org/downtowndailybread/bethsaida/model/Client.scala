package org.downtowndailybread.bethsaida.model

import java.time.LocalDate
import java.util.UUID

case class Client(
                   id: UUID,
                   firstName: String,
                   middleName: Option[String],
                   lastName: String,
                   nicknames: Seq[String],
                   dateOfBirth: LocalDate,
                   photoIdTag: String
                 )
