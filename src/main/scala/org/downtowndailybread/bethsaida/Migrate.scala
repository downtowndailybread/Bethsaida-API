package org.downtowndailybread.bethsaida

import java.time.{LocalDate, ZoneId}
import java.time.LocalDateTime
import java.time.Month

import spray.json.{JsNumber, JsString, JsValue}
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariDataSource
import org.downtowndailybread.bethsaida.model.parameters.{LoginParameters, UserParameters}
import org.downtowndailybread.bethsaida.request.{ClientRequest, EventRequest, ServiceRequest, UserRequest}
import org.flywaydb.core.Flyway
import org.downtowndailybread.bethsaida.providers.{DatabaseConnectionProvider, SettingsProvider, UUIDProvider}
import org.downtowndailybread.bethsaida.model._

object Migrate {

  def main(args: Array[String]): Unit = {
    val settings = new Settings(ConfigFactory.load("flyway"))
    migrate(settings.ds)
  }

  def migrate(ds: HikariDataSource): Unit = {
    val flyway = Flyway.configure.dataSource(ds).schemas("bethsaida").load()
    flyway.migrate()
  }
}


//object GenerateFakeData extends DatabaseConnectionProvider with SettingsProvider {
//  val settings = new Settings(ConfigFactory.load())
//  val connection = settings.ds.getConnection()
//
//  val clientOneAttributes = Map[String, Any](
//    "name" -> "Andy G",
//    "date_of_birth" -> LocalDate.parse("1989-12-11"),
//    "client_since" -> LocalDate.parse("2019-01-01"),
//    "number_of_children" -> 10
//  )
//
//  val clientTwoAttributes = Map[String, Any](
//    "name" -> "Tom Z",
//    "date_of_birth" -> LocalDate.parse("1989-09-02"),
//    "client_since" -> LocalDate.parse("2019-01-01"),
//    "ssn" -> "111-11-1111"
//  )
//
//
//
//  def runService(): Unit = {
//    implicit val user = AnonymousUser
//
//    val dayShelter = ServiceAttributes("Day Shelter", ServiceType.Recurring, None)
//    runSql(c => new ServiceRequest(settings, c).insertService(dayShelter))
//
//    val nightShelter = ServiceAttributes("Night Shelter", ServiceType.Recurring, None)
//    runSql(c => new ServiceRequest(settings, c).insertService(nightShelter))
//  }
//
//  def runEvent(): Unit = {
//    implicit val user: InternalUser = runSql(c => new UserRequest(settings, c)
//      .getRawUserFromEmail("user1@user.com")
//    )
//    val start = LocalDateTime.of(2019, Month.JANUARY, 1, 7, 0
//      ).atZone(ZoneId.of("America/New_York"))
//
//    val end = LocalDateTime.of(2020, Month.JANUARY, 1, 19, 0
//      ).atZone(ZoneId.of("America/New_York"))
//
//    val service = runSql(c => new ServiceRequest(settings, c).getAllServices()).head
//    val event = EventAttribute(
//      hours = HoursOfOperation(start, end),
//      capacity = None,
//      userCreatorId = Some(user.id),
//      scheduleCreatorId = None
//    )
//    runSql(c => new EventRequest(settings, c)
//      .createEvent(service.id, event)
//    )
//  }
//
//  def runUser(): Unit = {
//    implicit val anonymousUser: AnonymousUser.type = AnonymousUser
//    val user = UserParameters(
//      name = "user1",
//      loginParameters = LoginParameters(
//        email = "user1@user.com",
//        password = "password"
//      )
//    )
//    runSql(c => new UserRequest(settings, c).insertUser(user))
//  }
//
//  def main(args: Array[String]): Unit = {
//    val settings = new Settings(ConfigFactory.load())
////    val attributeTypes = runAttributeTypes(false)
////    runClients(attributeTypes)
////    runService()
////    runUser()
////    runEvent()
//
//  }
//}
//
//
//object ClientAttributeTypeGenerator {
//
//  val defaultName = "Cattribute"
//
//  case class AttributeParameters(
//    dataType: String,
//    required: Boolean,
//    displayName: Option[String] = None
//  )
//
//  private val uuidProvider = new UUIDProvider {}
//
//  def clientAttributeTypesToMap(attributes: Seq[ClientAttributeType]): Map[String, ClientAttributeType] = {
//    attributes.map(x => (x.id, x)).toMap
//  }
//
//  def createClientAttributeType(
//                                 name: Option[String],
//                                 dataType: String,
//                                 required: Boolean,
//                                 ordering: Int): ClientAttributeType = {
//    ClientAttributeType(
//      id = name match {
//        case Some(x) => x.replace(" ", "_").toLowerCase()
//        case None => s"${defaultName}_$ordering".toLowerCase()
//      },
//      clientAttributeTypeAttribute = ClientAttributeTypeAttribute(
//        displayName = name.getOrElse(s"$defaultName $ordering"),
//        dataType = dataType,
//        required = required,
//        requiredForOnboarding = false,
//        ordering = ordering
//      )
//    )
//  }
//
//  def run(attributeParameters: Seq[AttributeParameters]): Seq[ClientAttributeType] = {
//    attributeParameters.zipWithIndex.map(x => {
//      createClientAttributeType(
//        x._1.displayName, x._1.dataType, x._1.required, x._2
//      )
//    })
//  }
//}
//
//
//object ClientAttributeGenerator {
//  private val uuidProvider = new UUIDProvider {}
//
//  private def valueToJsValue(value: Any): JsValue = {
//    value match {
//      case x: String => JsString(x)
//      case x: Int => JsNumber(x)
//      case x: Double => JsNumber(x)
//      case x: LocalDate => JsString(x.toString)
//    }
//  }
//
//  def createClientAndAttributes(attributes: Map[ClientAttributeType, Any]): Seq[ClientAttribute] = {
//      attributes.map { case (k, v) => ClientAttribute(k.id, valueToJsValue(v)) }.toSeq
//  }
//
//  def createAttributeTypeMap(attributeTypeMap: Map[String, ClientAttributeType], attributes: Map[String, Any]): Option[Map[ClientAttributeType, Any]] = {
//    Some(attributes.map(x => (attributeTypeMap.get(x._1), x._2))
//      .filter(_._1.isDefined)
//      .map(x => (x._1.get, x._2)))
//  }
//
//  def createClientAttributes(attributeTypes: Seq[ClientAttributeType], attributes: Map[String, Any]): Option[(Seq[ClientAttribute])] = {
//    val attributeTypeMap = ClientAttributeTypeGenerator.clientAttributeTypesToMap(attributeTypes)
//    createAttributeTypeMap(attributeTypeMap, attributes).map(createClientAndAttributes)
//  }
//}
//
//
//object EventGenerator {
//
//}