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
                         phone: Option[Int],
                         clientPhoto: Option[String],
                         photoId: Option[String],
                         intakeDate: Option[LocalDate]
                 )
