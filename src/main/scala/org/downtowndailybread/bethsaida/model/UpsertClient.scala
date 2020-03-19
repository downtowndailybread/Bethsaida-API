package org.downtowndailybread.bethsaida.model

import java.time.LocalDate
import java.util.UUID

case class UpsertClient(
                         firstName: Option[String],
                         middleName: Option[String],
                         lastName: Option[String],
                         dateOfBirth: Option[LocalDate],
                         gender: Option[Gender],
                         race: Option[Race],
                         phone: Option[String],
                         clientPhoto: Option[UUID],
                         photoId: Option[UUID],
                         intakeDate: Option[LocalDate]
                 )
