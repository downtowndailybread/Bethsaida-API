package org.downtowndailybread.bethsaida.model

import java.time.LocalDate
import java.util.UUID

case class UpsertClient(
                   firstName: Option[String],
                   middleName: Option[String],
                   lastName: Option[String],
                   nicknames: Option[Seq[String]],
                   dateOfBirth: Option[LocalDate],
                   photoIdTag: Option[String]
                 )
