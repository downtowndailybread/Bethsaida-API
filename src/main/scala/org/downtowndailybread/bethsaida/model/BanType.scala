package org.downtowndailybread.bethsaida.model

import java.time.LocalDate

sealed abstract class BanType(val str: String)

object DateBan extends BanType("DB")
object IndefiniteBan extends BanType("IB")
object PermanentBan extends BanType("PB")


object BanType {
  val all = List(DateBan, IndefiniteBan, PermanentBan)

  def apply(str: String): BanType = {
    all.find(_.str == str.toUpperCase()).get
  }
}
