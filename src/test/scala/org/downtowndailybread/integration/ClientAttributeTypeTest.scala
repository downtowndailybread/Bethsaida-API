package org.downtowndailybread.integration

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import org.downtowndailybread.bethsaida.model.ClientAttributeType
import org.downtowndailybread.bethsaida.tag.IntegrationTest
import org.downtowndailybread.integration.base.BethsaidaSupport
import spray.json.{JsArray, JsBoolean, JsNumber, JsObject, JsString}

trait ClientAttributeTypeTest {
  this: BethsaidaSupport =>

  "a user" should "be able to get a list of all attribute types" taggedAs IntegrationTest in {
    Get(apiBaseUrl + "/clientAttributeType").authenticate() ~> routes ~> check {
      assert(status == StatusCodes.OK)
      assert(responseAs[JsArray].elements.isEmpty)
    }
  }

  "an unauthenticated user" should "not be able to get a list of all attribute types" taggedAs IntegrationTest in {
    Get(apiBaseUrl + "/clientAttributeType") ~> routes ~> check {
      assert(status == StatusCodes.Unauthorized)
    }
  }

  "an unauth user" should "not be able to insert ClientAttributeTypes" taggedAs IntegrationTest in {
    Post(apiBaseUrl + "/clientAttributeType/new").withEntity(ContentTypes.`application/json`,
      JsArray(
        JsObject(
          ("id", JsString("first_name")),
          ("name", JsString("first_name")),
          ("datatype", JsString("string")),
          ("required", JsBoolean(true)),
          ("requiredForOnboarding", JsBoolean(true)),
          ("ordering", JsNumber(0))
        )
      ).toString
    ) ~> routes ~> check {
      assert(status == StatusCodes.Unauthorized)
    }
  }


  "an authenticated user" should "be able to insert ClientAttributeTypes" taggedAs IntegrationTest in {
    Post(apiBaseUrl + "/clientAttributeType/new").withEntity(ContentTypes.`application/json`,
      JsArray(
        JsObject(
          ("id", JsString("first_name")),
          ("name", JsString("First Name")),
          ("datatype", JsString("string")),
          ("required", JsBoolean(true)),
          ("requiredForOnboarding", JsBoolean(true)),
          ("ordering", JsNumber(0))
        ),
        JsObject(
          ("id", JsString("last_name")),
          ("name", JsString("Last Name")),
          ("datatype", JsString("string")),
          ("required", JsBoolean(true)),
          ("requiredForOnboarding", JsBoolean(true)),
          ("ordering", JsNumber(1))
        ),
        JsObject(
          ("id", JsString("middle_name")),
          ("name", JsString("Middle Name")),
          ("datatype", JsString("string")),
          ("required", JsBoolean(false)),
          ("requiredForOnboarding", JsBoolean(false)),
          ("ordering", JsNumber(2))
        ),
        JsObject(
          ("id", JsString("dob")),
          ("name", JsString("Date of Birth")),
          ("datatype", JsString("date")),
          ("required", JsBoolean(true)),
          ("requiredForOnboarding", JsBoolean(true)),
          ("ordering", JsNumber(3))
        ),
        JsObject(
          ("id", JsString("ssn")),
          ("name", JsString("Last 4 SSN")),
          ("datatype", JsString("string")),
          ("required", JsBoolean(true)),
          ("requiredForOnboarding", JsBoolean(true)),
          ("ordering", JsNumber(4))
        ),
        JsObject(
          ("id", JsString("veteran")),
          ("name", JsString("Veteran Status")),
          ("datatype", JsString("true")),
          ("required", JsBoolean(false)),
          ("requiredForOnboarding", JsBoolean(true)),
          ("ordering", JsNumber(5))
        ),
        JsObject(
          ("id", JsString("hair_color")),
          ("name", JsString("Color of hair")),
          ("datatype", JsString("string")),
          ("required", JsBoolean(false)),
          ("requiredForOnboarding", JsBoolean(false)),
          ("ordering", JsNumber(6))
        )
      ).toString
    ).authenticate() ~> routes ~> check {
      assert(status == StatusCodes.Created)
    }
  }

  "the attributes" should "have been correctly inserted" taggedAs IntegrationTest in {
    Get(apiBaseUrl + "/clientAttributeType").authenticate() ~> routes ~> check {
      assert(status == StatusCodes.OK)
      val resp = responseAs[Seq[ClientAttributeType]]
      assert(resp.size == 7)
      assert(Set("first_name", "last_name", "middle_name", "dob", "ssn", "hair_color", "veteran")
        .forall(resp.map(_.id).contains))
    }
  }

  "attributes" should "not be updatable by unauthorized users" taggedAs IntegrationTest in {
    Get(apiBaseUrl + "/clientAttributeType").authenticate() ~> routes ~> check {
      assert(status == StatusCodes.OK)
      val resp = responseAs[Seq[ClientAttributeType]]
      assert(
        resp.find(_.id == "first_name").exists(a =>
          a.clientAttributeTypeAttribute.displayName == "First Name" &&
            a.clientAttributeTypeAttribute.dataType == "string" &&
            a.clientAttributeTypeAttribute.required &&
            a.clientAttributeTypeAttribute.requiredForOnboarding &&
            a.clientAttributeTypeAttribute.ordering == 0
        )
      )
    }
    Post(apiBaseUrl + "/clientAttributeType/first_name/update").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("name", JsString("Date of insertion")),
        ("datatype", JsString("date")),
        ("required", JsBoolean(false)),
        ("requiredForOnboarding", JsBoolean(false)),
        ("ordering", JsNumber(10))
      ).toString) ~> routes ~> check {
      assert(status == StatusCodes.Unauthorized)
    }
  }

  "attributes" should "be updatable by authorized users" taggedAs IntegrationTest in {
    Get(apiBaseUrl + "/clientAttributeType").authenticate() ~> routes ~> check {
      assert(status == StatusCodes.OK)
      val resp = responseAs[Seq[ClientAttributeType]]
      assert(
        resp.find(_.id == "first_name").exists(a =>
          a.clientAttributeTypeAttribute.displayName == "First Name" &&
            a.clientAttributeTypeAttribute.dataType == "string" &&
            a.clientAttributeTypeAttribute.required &&
            a.clientAttributeTypeAttribute.requiredForOnboarding &&
            a.clientAttributeTypeAttribute.ordering == 0
        )
      )
    }
    Post(apiBaseUrl + "/clientAttributeType/first_name/update").authenticate()
      .withEntity(ContentTypes.`application/json`,
        JsObject(
          ("name", JsString("aaaaa")),
          ("datatype", JsString("date")),
          ("required", JsBoolean(false)),
          ("requiredForOnboarding", JsBoolean(false)),
          ("ordering", JsNumber(15))
        ).toString).authenticate ~> routes ~> check {
      assert(status == StatusCodes.OK)
    }
    Get(apiBaseUrl + "/clientAttributeType").authenticate() ~> routes ~> check {
      assert(status == StatusCodes.OK)
      val resp = responseAs[Seq[ClientAttributeType]]
      assert(
        resp.find(_.id == "first_name").exists(a =>
          a.clientAttributeTypeAttribute.displayName == "aaaaa" &&
            a.clientAttributeTypeAttribute.dataType == "date" &&
            !a.clientAttributeTypeAttribute.required &&
            !a.clientAttributeTypeAttribute.requiredForOnboarding &&
            a.clientAttributeTypeAttribute.ordering == 15
        )
      )
    }
    Post(apiBaseUrl + "/clientAttributeType/first_name/update")
      .withEntity(ContentTypes.`application/json`,
        JsObject(
          ("name", JsString("First Name")),
          ("datatype", JsString("string")),
          ("required", JsBoolean(true)),
          ("requiredForOnboarding", JsBoolean(true)),
          ("ordering", JsNumber(0))
        ).toString).authenticate ~> routes ~> check {
      assert(status == StatusCodes.OK)
    }
  }

  "attribute id" should "not be updatable" taggedAs IntegrationTest in {
    Post(apiBaseUrl + "/clientAttributeType/first_name/update").withEntity(ContentTypes.`application/json`,
      JsObject(
        ("id", "fake_id"),
        ("name", JsString("First Name")),
        ("datatype", JsString("string")),
        ("required", JsBoolean(true)),
        ("requiredForOnboarding", JsBoolean(true)),
        ("ordering", JsNumber(0))
      ).toString).authenticate() ~> routes ~> check {
      assert(status == StatusCodes.BadRequest)
    }
    Get(apiBaseUrl + "/clientAttributeType").authenticate() ~> routes ~> check {
      assert(status == StatusCodes.OK)
      val resp = responseAs[Seq[ClientAttributeType]]
      assert(!resp.exists(_.id == "fake_id"))
    }
  }

  "attributes" should "not be able to be deleted by unauthorized users" taggedAs IntegrationTest in {
    Post(apiBaseUrl + "/clientAttributeType/first_name/delete") ~> routes ~> check {
      assert(status == StatusCodes.Unauthorized)
    }
  }

  "attributes" should "be able to be deleted by unauthorized users" taggedAs IntegrationTest in {
    Post(apiBaseUrl + "/clientAttributeType/new").withEntity(ContentTypes.`application/json`,
      JsArray(
        JsObject(
          ("id", JsString("fake_point")),
          ("name", JsString("First Name")),
          ("datatype", JsString("string")),
          ("required", JsBoolean(true)),
          ("requiredForOnboarding", JsBoolean(true)),
          ("ordering", JsNumber(0))
        )
      ).toString
    ).authenticate() ~> routes ~> check {
      assert(status == StatusCodes.Created)
    }
    Post(apiBaseUrl + "/clientAttributeType/fake_point/delete").authenticate ~> routes ~> check {
      assert(status == StatusCodes.OK)
    }
  }
}
