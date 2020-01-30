package org.downtowndailybread.integration


import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import org.downtowndailybread.bethsaida.model.Client
import org.downtowndailybread.bethsaida.tag.IntegrationTest
import org.downtowndailybread.integration.base.BethsaidaSupport
import spray.json.{JsArray, JsObject}

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

  "a client" should "be able to be inserted by an authenticated user" taggedAs IntegrationTest in {
    Get(apiBaseUrl + "/client").authenticate() ~> routes ~> check {
      assert(status == StatusCodes.OK)
      assert(responseAs[Seq[Client]].isEmpty)
    }
    Post(apiBaseUrl + "/client/new").withEntity(ContentTypes.`application/json`,
      JsArray(
        JsObject(
          ("id", "first_name"),
          ("value", "Andy")
        ),
        JsObject(
          ("id", "last_name"),
          ("value", "Guenin")
        ),
        JsObject(
          ("id", "dob"),
          ("value", "1990-01-01")
        ),
        JsObject(
          ("id", "ssn"),
          ("value", "0000")
        )
      ).toString
    ).authenticate() ~> routes ~> check {
      assert(status == StatusCodes.Created)
    }
    Get(apiBaseUrl + "/client").authenticate() ~> routes ~> check {
      assert(status == StatusCodes.OK)
      println(responseAs[String])
      assert(responseAs[Seq[Client]].size == 1)
    }
  }

  "a client" should "be able to be fetched" taggedAs IntegrationTest in {
    Get(apiBaseUrl + "/client").authenticate() ~> routes ~> check {
      assert(status == StatusCodes.OK)
    }
  }
}
