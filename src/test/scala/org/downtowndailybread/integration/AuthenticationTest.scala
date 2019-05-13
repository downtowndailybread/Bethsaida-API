package org.downtowndailybread.integration

import java.util.UUID

import spray.json._
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import org.downtowndailybread.bethsaida.request.UserRequest
import org.downtowndailybread.integration.base.BethsaidaSupport

import scala.concurrent._
import duration._

trait AuthenticationTest extends BethsaidaSupport {

  private val resetToken = Promise[UUID]()

  "get all user" should "require authentication" in {
    Get(apiBaseUrl + "/user") ~> routes ~> check {
      assert(status == StatusCodes.Unauthorized)
    }
  }

  "creating a new user" should "work" in {
    Post(apiBaseUrl + "/user/new").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("name", JsString(userParams.name)),
        ("email", JsString(userParams.loginParameters.email)),
        ("password", JsString(userParams.loginParameters.password + "DD"))
      ).toString
      ) ~> routes ~> check {
      assert(status == StatusCodes.Created)
    }
  }

  "creating a duplicate user" should "not work" in {
    Post(apiBaseUrl + "/user/new").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("name", JsString(userParams.name)),
        ("email", JsString(userParams.loginParameters.email)),
        ("password", JsString(userParams.loginParameters.password + "DD"))
      ).toString
    ) ~> routes ~> check {
      assert(status == StatusCodes.BadRequest)
      val response = responseAs[JsValue] match {
        case JsObject(o) => o("error").convertTo[String]
      }
      assert(response == "EmailAlreadyExists")
    }
  }

  "a user that hasn't been confirmed" should "not be able to login" in {
    Post(apiBaseUrl + "/authenticate").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("email", JsString(userParams.loginParameters.email)),
        ("password", JsString(userParams.loginParameters.password + "DD"))
      ).toString()
    ) ~> routes ~> check {
      assert(status == StatusCodes.BadRequest)
    }
  }

  "confirming a user with the wrong token" should "not work" in {
    val ac = new UserRequest(settings, settings.ds.getConnection).getRawUserFromEmail(userParams.loginParameters.email)
    Post(apiBaseUrl + "/authenticate/confirm").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("email", JsString(userParams.loginParameters.email)),
        ("token", JsString(getUUID()))
      ).toString
    ) ~> routes ~> check {
      assert(status == StatusCodes.NotFound)
    }
  }

  "confirming a user with the wrong email" should "not work" in {
    val ac = new UserRequest(settings, settings.ds.getConnection).getRawUserFromEmail(userParams.loginParameters.email)
    Post(apiBaseUrl + "/authenticate/confirm").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("email", JsString(userParams.loginParameters.email + ".othercom")),
        ("token", JsString(getUUID()))
      ).toString
    ) ~> routes ~> check {
      assert(status == StatusCodes.NotFound)
    }
  }

  "a user that hasn't been confirmed but has tried with two failed attempts" should "not be able to login" in {
    Post(apiBaseUrl + "/authenticate").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("email", JsString(userParams.loginParameters.email)),
        ("password", JsString(userParams.loginParameters.password + "DD"))
      ).toString()
    ) ~> routes ~> check {
      assert(status == StatusCodes.BadRequest)
    }
  }

  "confirming a user" should "work" in {
    val ac = new UserRequest(settings, settings.ds.getConnection).getRawUserFromEmail(userParams.loginParameters.email)
    Post(apiBaseUrl + "/authenticate/confirm").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("email", JsString(userParams.loginParameters.email)),
        ("token", ac.resetToken.map(_.toString))
      ).toString
    ) ~> routes ~> check {
      assert(status == StatusCodes.OK)
    }
  }

  "a user that has been confirmed" should "be able to login" in {
    Post(apiBaseUrl + "/authenticate").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("email", JsString(userParams.loginParameters.email)),
        ("password", JsString(userParams.loginParameters.password + "DD"))
      ).toString()
    ) ~> routes ~> check {
      assert(status == StatusCodes.OK)
    }
  }

  "a user that has been confirmed" should "not have a reset token set" in {
    val user =
      new UserRequest(settings, settings.ds.getConnection).getRawUserFromEmail(userParams.loginParameters.email)
    assert(user.resetToken.isEmpty)
  }


  "a user that initiates a password reset" should "have a reset token set" in {
    Post(apiBaseUrl + "/authenticate/initiatePasswordReset").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("email", JsString(userParams.loginParameters.email))
      ).toString) ~> routes ~> check {
      assert(status == StatusCodes.OK)
    }
    val user =
      new UserRequest(settings, settings.ds.getConnection).getRawUserFromEmail(userParams.loginParameters.email)
    assert(user.resetToken.nonEmpty)
    resetToken.success(user.resetToken.get)
  }

  "a user requesting a password reset with the wrong reset token" should "fail" in {
    Post(apiBaseUrl + "/authenticate/resetPassword").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("email", JsString(userParams.loginParameters.email)),
        ("token", JsString(getUUID().toString)),
        ("password", JsString(userParams.loginParameters.password))
      ).toString
    ) ~> routes ~> check {
      assert(status == StatusCodes.BadRequest)
    }
  }

  "a user requesting a password reset with the right reset token but wrong email" should "fail" in {
    val token = Await.result(resetToken.future, 1.second)
    Post(apiBaseUrl + "/authenticate/resetPassword").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("email", JsString(userParams.loginParameters.email + ".fail")),
        ("token", JsString(token)),
        ("password", JsString(userParams.loginParameters.password))
      ).toString
    ) ~> routes ~> check {
      assert(status == StatusCodes.BadRequest)
    }
  }

  "a user requesting a password reset with the right reset token and email" should "succeed" in {
    val token = Await.result(resetToken.future, 1.second)
    Post(apiBaseUrl + "/authenticate/resetPassword").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("email", JsString(userParams.loginParameters.email)),
        ("token", JsString(token)),
        ("password", JsString(userParams.loginParameters.password))
      ).toString
    ) ~> routes ~> check {
      assert(status == StatusCodes.OK)
    }
  }

  "a user that has reset its password" should "be able to login" in {
    Post(apiBaseUrl + "/authenticate").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("email", JsString(userParams.loginParameters.email)),
        ("password", JsString(userParams.loginParameters.password))
      ).toString()
    ) ~> routes ~> check {
      assert(status == StatusCodes.OK)
    }
  }

  "a user that has reset its password" should "not have a reset token" in {
    val user =
      new UserRequest(settings, settings.ds.getConnection).getRawUserFromEmail(userParams.loginParameters.email)
    assert(user.resetToken.isEmpty)
  }
}
