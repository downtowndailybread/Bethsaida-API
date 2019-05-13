package org.downtowndailybread.bethsaida

import com.typesafe.config.ConfigFactory
import org.downtowndailybread.bethsaida.request.ClientAttributeTypeRequest
import org.flywaydb.core.Flyway
import org.downtowndailybread.bethsaida.providers.UUIDProvider
import org.downtowndailybread.bethsaida.model.{AnonymousUser, ClientAttributeType, ClientAttributeTypeAttribute}
import org.downtowndailybread.bethsaida.request.util.DatabaseSource

object Migrate {

  def main(args: Array[String]): Unit = {
    val flyway = Flyway.configure.dataSource(DatabaseSource.ds).load()
    flyway.migrate()
  }
}


object GenerateFakeData {
  val connection = DatabaseSource.ds.getConnection()

  def createClientAttributeTypes(settings: Settings): Seq[ClientAttributeType] = {
    import ClientAttributeTypeGenerator.AttributeParameters
    val attributes = Seq(
      AttributeParameters("string", true, Some("name")),
      AttributeParameters("string", true),
      AttributeParameters("string", false),
      AttributeParameters("string", false),
      AttributeParameters("string", false),
    )

    val clientAttributeTypes = ClientAttributeTypeGenerator.run(attributes)
    clientAttributeTypes.foreach{
      r =>
      DatabaseSource.runSql(c =>
        new ClientAttributeTypeRequest(c, settings).insertClientAttributeType(r)(AnonymousUser))
    }
    clientAttributeTypes
  }

  def main(args: Array[String]): Unit = {
    createClientAttributeTypes(new Settings(ConfigFactory.load()))
  }
}


object ClientAttributeTypeGenerator {

  val defaultName = "Cattribute"

  case class AttributeParameters(
    dataType: String,
    required: Boolean,
    displayName: Option[String] = None
  )
  private val uuidProvider = new UUIDProvider {}

  def createClientAttributeType(
      name: Option[String],
      dataType: String,
      required: Boolean,
      ordering: Int): ClientAttributeType = {
    ClientAttributeType(
      id = name.getOrElse(s"${defaultName}_${ordering}").toLowerCase(),
      clientAttributeTypeAttribute = ClientAttributeTypeAttribute(
        displayName = name.getOrElse(s"$defaultName $ordering"),
        dataType = dataType,
        required = required,
        requiredForOnboarding = false,
        ordering = ordering
      )
    )
  }

 def run(attributeParameters: Seq[AttributeParameters]): Seq[ClientAttributeType] = {
   attributeParameters.zipWithIndex.map(x => {
     createClientAttributeType(
       x._1.displayName, x._1.dataType, x._1.required, x._2
     )
   })
 }

}