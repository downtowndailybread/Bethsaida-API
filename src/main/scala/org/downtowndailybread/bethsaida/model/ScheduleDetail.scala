package org.downtowndailybread.bethsaida.model

import java.time.{LocalDateTime, LocalTime, ZonedDateTime}
import java.util.TimeZone

import org.dmfs.rfc5545.DateTime
import org.dmfs.rfc5545.recur.RecurrenceRule

case class ScheduleDetail(
                           rrule: String,
                           beginTime: LocalTime,
                           endTime: LocalTime,
                           enabled: Boolean
                         ) {

  private val rule = new RecurrenceRule(if(rrule.startsWith("RRULE:")) rrule.substring("RRULE:".size) else rrule)

  private val endNextDay = !beginTime.isBefore(endTime)

  def getNextNInstances(n: Int)(implicit now: LocalDateTime): Seq[HoursOfOperation] = {
    if (enabled) {
      val tz = TimeZone.getDefault
      val localDateTime = {
        val tmp = LocalDateTime.of(now.toLocalDate, beginTime)
        if (tmp.isBefore(now)) {
          tmp.plusDays(1)
        } else {
          tmp
        }
      }.atZone(tz.toZoneId)

      val timeIter = rule.iterator(new DateTime(tz, localDateTime.toEpochSecond * 1000))

      val iter = new Iterator[DateTime] {
        override def hasNext: Boolean = timeIter.hasNext

        override def next(): DateTime = timeIter.nextDateTime()
      }
      iter.take(n).map(r =>
        LocalDateTime.of(
          r.getYear, r.getMonth + 1, r.getDayOfMonth, r.getHours, r.getMinutes, r.getSeconds
        ).atZone(tz.toZoneId)
      ).toStream.map {
        dt => HoursOfOperation(dt, LocalDateTime.of(dt.toLocalDate.plusDays(if (endNextDay) 1 else 0), LocalTime.of(
          endTime.getHour, endTime.getMinute, endTime.getSecond
        )).atZone(tz.toZoneId))
      }.take(n).toVector
    }
    else {
      Seq()
    }
  }

}
