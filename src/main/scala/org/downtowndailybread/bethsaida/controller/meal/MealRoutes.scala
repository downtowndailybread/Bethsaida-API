package org.downtowndailybread.bethsaida.controller.meal

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, MaterializerProvider, S3Provider}

trait MealRoutes extends Update with Get {
  this: AuthenticationProvider with JsonSupport with DatabaseConnectionProvider with MaterializerProvider with S3Provider =>

  val allMealRoutes = pathPrefix("meal") {
    meal_updateRoute ~ meal_getRoute
  }
}


