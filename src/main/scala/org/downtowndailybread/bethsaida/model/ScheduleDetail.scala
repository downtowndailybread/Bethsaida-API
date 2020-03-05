//package org.downtowndailybread.bethsaida.model
//
//import java.time.{LocalDateTime, LocalTime, ZonedDateTime}
//import java.util.TimeZone
//
//import org.dmfs.rfc5545.DateTime
//import org.dmfs.rfc5545.recur.RecurrenceRule
//
//case class ScheduleDetail(
//                           rrule: String,
//                           startTime: LocalTime,
//                           endTime: LocalTime,
//                           scheduleCapacity: Option[Int],
//                           enabled: Boolean
//                         ) {
//
//  private val rule = new RecurrenceRule(if(rrule.startsWith("RRULE:")) rrule.substring("RRULE:".size) else rrule)
//
//  private val endNextDay = !startTime.isBefore(endTime)
//
//  def getSchedules(implicit now: ZonedDateTime): Stream[HoursOfOperation] = {
//    if (enabled) {
//      val tz = TimeZone.getDefault
//      val localDateTime = {
//        val tmp = ZonedDateTime.of(LocalDateTime.of(now.toLocalDate, startTime), tz.toZoneId)
//        if (tmp.isBefore(now)) {
//          tmp.plusDays(1)
//        } else {
//          tmp
//        }
//      }
//
//      val timeIter = rule.iterator(new DateTime(tz, localDateTime.toEpochSecond * 1000))
//
//      val stream = new Iterator[DateTime] {
//        override def hasNext: Boolean = timeIter.hasNext
//
//        override def next(): DateTime = timeIter.nextDateTime()
//      }.toStream
//
//      stream.map(r =>
//        LocalDateTime.of(
//          r.getYear, r.getMonth + 1, r.getDayOfMonth, r.getHours, r.getMinutes, r.getSeconds
//        ).atZone(tz.toZoneId)
//      ).map {
//        dt => HoursOfOperation(dt, LocalDateTime.of(dt.toLocalDate.plusDays(if (endNextDay) 1 else 0), LocalTime.of(
//          endTime.getHour, endTime.getMinute, endTime.getSecond
//        )).atZone(tz.toZoneId))
//      }
//    }
//    else {
//      Stream()
//    }
//  }
//
//  def getNextNInstances(n: Int)(implicit now: ZonedDateTime): Seq[HoursOfOperation] = {
//    getSchedules.take(n).toList
//  }
//}
