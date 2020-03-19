package org.downtowndailybread.bethsaida.model

import java.time.LocalDateTime

case class BanAttribute(
                         startTime: LocalDateTime,
                         stopTime: Option[LocalDateTime],
                         banType: BanType,
                         notes: Option[String]
                       ) {
  def isActive(date: LocalDateTime): Boolean = {
    val onOrAfter = !date.isBefore(startTime)
    stopTime match {
      case Some(banDateEnd) => onOrAfter && !date.isAfter(banDateEnd)
      case _ => onOrAfter
    }
  }
}
