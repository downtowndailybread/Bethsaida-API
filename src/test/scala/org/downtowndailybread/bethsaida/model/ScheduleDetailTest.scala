package org.downtowndailybread.bethsaida.model

import org.scalatest.FlatSpec
import java.time.{LocalDateTime, LocalTime}
import java.util.TimeZone

class ScheduleDetailTest extends FlatSpec {

  implicit val time = LocalDateTime.of(2020, 1, 5, 5, 0, 0)
    .atZone(TimeZone.getDefault.toZoneId)
  val startTime = LocalTime.of(11, 0, 0)
  val endTime = LocalTime.of(13, 0, 0)

  "a scheudle that includes the RRULE: prefix" should "work" in {
    val schedule = ScheduleDetail(
      "RRULE:FREQ=DAILY",
      startTime,
      endTime,
      None,
      true
    )

    val nextScheduled = schedule.getNextNInstances(1).head

    assert(nextScheduled.start.toLocalDateTime == LocalDateTime.of(2020, 1, 5, 11, 0, 0))
    assert(nextScheduled.end.toLocalDateTime == LocalDateTime.of(2020, 1, 5, 13, 0, 0))
  }

  "a simple schedule" should "return valid ranges" in {
    val schedule = ScheduleDetail(
      "RRULE:FREQ=DAILY",
      startTime,
      endTime,
      None,
      true
    )

    val nextScheduled = schedule.getNextNInstances(1).head

    assert(nextScheduled.start.toLocalDateTime == LocalDateTime.of(2020, 1, 5, 11, 0, 0))
    assert(nextScheduled.end.toLocalDateTime == LocalDateTime.of(2020, 1, 5, 13, 0, 0))
  }

  "a schedule that passes the midnight boundary" should "return the correct range" in {
    assert(true)
    val schedule = ScheduleDetail(
      "RRULE:FREQ=DAILY",
      startTime,
      endTime.plusHours(12),
      None,
      true
    )

    val nextScheduled = schedule.getNextNInstances(1).head
    assert(nextScheduled.start.toLocalDateTime == LocalDateTime.of(2020, 1, 5, 11, 0, 0))
    assert(nextScheduled.end.toLocalDateTime == LocalDateTime.of(2020, 1, 6, 1, 0, 0))
  }

  "a schedule that has a start time before the current time" should "start tomorrow" in {
    assert(true)
    val schedule = ScheduleDetail(
      "RRULE:FREQ=DAILY",
      startTime.minusHours(8),
      endTime,
      None,
      true
    )

    val nextScheduled = schedule.getNextNInstances(1).head
    assert(nextScheduled.start.toLocalDateTime == LocalDateTime.of(2020, 1, 6, 3, 0, 0))
    assert(nextScheduled.end.toLocalDateTime == LocalDateTime.of(2020, 1, 6, 13, 0, 0))
  }
}
