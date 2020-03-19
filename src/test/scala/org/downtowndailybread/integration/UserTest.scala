package org.downtowndailybread.integration

import java.util.UUID

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.RouteTestTimeout
import org.downtowndailybread.bethsaida.model.InternalUser
import org.downtowndailybread.bethsaida.model.parameters.{LoginParameters, UserParameters}
import org.downtowndailybread.bethsaida.request.UserRequest
import org.downtowndailybread.bethsaida.tag.IntegrationTest
import org.downtowndailybread.integration.base.BethsaidaSupport
import spray.json.JsObject

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}

trait UserTest {
  this: BethsaidaSupport =>

  private val otherUserParameters = UserParameters(
    "a b",
    LoginParameters(
      "nobody@nobody.com",
      "initialPassword"
    ),
    None
  )

  private val otherUserPromise = Promise[UUID]()
  lazy val otherUser = new UserRequest(settings, settings.ds.getConnection).getRawUserFromUuid(
    Await.result(otherUserPromise.future, 1.second)
  )

  lazy val loggedInUser = new UserRequest(settings, settings.ds.getConnection).getRawUserFromEmail(
    userParams.loginParameters.email
  )

  "all user routes" should "fail without authentication" taggedAs IntegrationTest in {
    Get(apiBaseUrl + "/user") ~> routes ~> check {
      assert(status == StatusCodes.Unauthorized)
    }
  }

  "a user" should "be able to get the list of users" taggedAs IntegrationTest in {
    val a = Get(apiBaseUrl + "/user")
    Get(apiBaseUrl + "/user").authenticate() ~> routes ~> check {
      assert(status == StatusCodes.OK)
      val r = responseAs[Seq[InternalUser]]
      assert(r.nonEmpty)
    }
  }

  "a user" should "be able to create a new user" taggedAs IntegrationTest in {
    Post(apiBaseUrl + "/user/new").authenticate().withEntity(ContentTypes.`application/json`, JsObject(
      ("email", otherUserParameters.loginParameters.email),
      ("name", otherUserParameters.name),
      ("password", otherUserParameters.loginParameters.password)
    ).toString) ~> routes ~> check {
      assert(status == StatusCodes.Created)
      val response = parseUUID(responseAs[JsObject].fields("id").convertTo[String])
      otherUserPromise.success(response)

    }
  }

  // Eventually, we will want only site admins to be able to create users. Not for now though.
  //  "a nonuser" should "not be able to create a user" in {
  //    Post(apiBaseUrl + "/user/new").withEntity(ContentTypes.`application/json`, JsObject(
  //      ("email", "nobody2@nobody.com"),
  //      ("name", "a b"),
  //      ("password", "initialpassword")
  //    ).toString) ~> routes ~> check {
  //      assert(status == StatusCodes.Unauthorized)
  //    }
  //  }

  "a user" should "be able to view the profile of another user" taggedAs IntegrationTest in {
    Get(apiBaseUrl + s"/user/${otherUser.id}").authenticate() ~> routes ~> check {
      assert(status == StatusCodes.OK)
    }
  }

  "a non-user" should "not be able to view the profile of another user" taggedAs IntegrationTest in {
    Get(apiBaseUrl + s"/user/${otherUser.id}") ~> routes ~> check {
      assert(status == StatusCodes.Unauthorized)
    }
  }

  //  implicit val timeout = RouteTestTimeout(5.seconds)

  "a user" should "be able to update their profile" taggedAs IntegrationTest in {
    Post(apiBaseUrl + s"/user/${loggedInUser.id}/update").authenticate()
      .withEntity(ContentTypes.`application/json`,
        JsObject(
          ("name", "Andy Guenin II"),
          ("email", "newemail@andy.com"),
          ("password", "mynewpassword"),
        ).toString()
      ) ~>
      routes ~> check {
      assert(status == StatusCodes.OK)
    }
    Post(apiBaseUrl + s"/user/${loggedInUser.id}/update").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("name", userParams.name),
        ("email", userParams.loginParameters.email),
        ("password", userParams.loginParameters.password),
      ).toString()
    ).authenticate() ~> routes ~> check {
      assert(status == StatusCodes.OK)
    }
  }

  "a user" should "not be able to update another profile" taggedAs IntegrationTest in {
    Post(apiBaseUrl + s"/user/${otherUser.id}/update").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("name", "Andy Guenin II"),
        ("email", "newemail@andy.com"),
        ("password", "mynewpassword"),
      ).toString()
    ) ~> routes ~> check {
      assert(status == StatusCodes.Unauthorized)
    }
  }


}
