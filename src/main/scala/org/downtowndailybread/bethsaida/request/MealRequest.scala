package org.downtowndailybread.bethsaida.request

import org.downtowndailybread.bethsaida.Settings
import org.downtowndailybread.bethsaida.model.{Mail, MailDetails, MealDetails}
import org.downtowndailybread.bethsaida.providers.UUIDProvider
import org.downtowndailybread.bethsaida.request.util.{BaseRequest, DatabaseRequest}

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.LocalDateTime
import java.util.UUID

class MealRequest(val settings: Settings, val conn: Connection)
  extends BaseRequest
    with DatabaseRequest
    with UUIDProvider {


  def getMeal(date: LocalDateTime): MealDetails = {

    val sql =
      s"""
         |select m.id, m.id, m.date, m.breakfast, m.lunch
         |from meal m
         |where m.date = ?
         |""".stripMargin


    val ps = conn.prepareStatement(sql)

    ps.setLocalDateTime(1, date)

    val result = ps.executeQuery()

    val rsConverter = (rs: ResultSet) => MealDetails(
      result.getUUID("id"),
      result.getLocalDateTime("date"),
      result.getInt("breakfast"),
      result.getInt("lunch")
    )

    if (result.next()) {
      rsConverter(result)
    } else {
      insertMeal(date, 0, 0)
    }
  }

  private def insertMeal(date: LocalDateTime, breakfast: Int, lunch: Int): MealDetails = {
    val meal = MealDetails(UUID.randomUUID(), date, breakfast, lunch)
    val insert =
      """
        |insert into meal
        |(id, date, breakfast, lunch)
        |values (cast(? as uuid), ?, ?, ?)""".stripMargin
    val ps = conn.prepareStatement(insert)
    ps.setUUID(1, meal.id)
    ps.setLocalDateTime(2, meal.date)
    ps.setInt(3, meal.breakfast)
    ps.setInt(4, meal.lunch)
    ps.executeUpdate()
    meal
  }

  def putMeal(mealDetails: MealDetails): UUID = {
    val update =
      """
        |update meal
        |set breakfast = ?, lunch = ?
        |where id = cast(? as uuid)""".stripMargin
    val ps = conn.prepareStatement(update)
    ps.setInt(1, mealDetails.breakfast)
    ps.setInt(2, mealDetails.lunch)
    ps.setUUID(3, mealDetails.id)
    ps.executeUpdate()
    mealDetails.id
  }
}
