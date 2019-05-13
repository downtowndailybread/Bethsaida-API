package org.downtowndailybread.integration


import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import org.downtowndailybread.integration.base.BethsaidaSupport
import spray.json._

import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._

trait ClientTest extends BethsaidaSupport {

  "a client" should "be able to be fetched" in {
    Post(apiBaseUrl + "/authenticate").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("email", JsString(userParams.loginParameters.email)),
        ("password", JsString(userParams.loginParameters.password))
      ).toString()
    ) ~> routes ~> check {
      assert(status == StatusCodes.OK)
      val r = responseAs[JsObject].fields("auth_token").convertTo[String]
      println(r)
      authTokenPromise.success(r)
    }
  }
}
