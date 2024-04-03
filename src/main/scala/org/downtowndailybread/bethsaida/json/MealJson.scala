package org.downtowndailybread.bethsaida.json

import org.downtowndailybread.bethsaida.model.{MealDetails}
import org.downtowndailybread.bethsaida.providers.SettingsProvider
import spray.json.DefaultJsonProtocol._

import java.util.UUID

trait MealJson extends BaseSupport {
    this: SettingsProvider =>

  implicit val mealDetailJson = jsonFormat4(MealDetails)

}
