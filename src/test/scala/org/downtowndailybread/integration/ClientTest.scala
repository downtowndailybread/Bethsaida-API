package org.downtowndailybread.integration


import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import org.downtowndailybread.bethsaida.tag.IntegrationTest
import org.downtowndailybread.integration.base.BethsaidaSupport
import spray.json.JsArray

trait ClientTest {

  this: BethsaidaSupport =>

  "all clients" should "return nothing for an authenticated user" taggedAs IntegrationTest in {
    Get(apiBaseUrl + "/client").authenticate() ~> routes ~> check {
      assert(status == StatusCodes.OK)
      val r = responseAs[JsArray]
      assert(r.elements.isEmpty)
    }
  }

  "all clients" should "return error for an unauthenticated user" taggedAs IntegrationTest in {
    Get(apiBaseUrl + "/client") ~> routes ~> check {
      assert(status == StatusCodes.Unauthorized)
    }
  }

//    "a client" should "be able to be inserted by an authenticated user" taggedAs IntegrationTest in {
//      Post(apiBaseUrl + "/client/new").withEntity(ContentTypes.`application/json`,
//        JsObject(
//
//        ).toString
//      )
//    }

  "a client" should "be able to be fetched" taggedAs IntegrationTest in {
    Get(apiBaseUrl + "/client").authenticate() ~> routes ~> check {
      assert(status == StatusCodes.OK)
    }
  }
}
