package org.downtowndailybread.bethsaida

import java.util.{Properties, TimeZone}

import com.typesafe.config.{Config, ConfigFactory}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

class Settings( val args: Array[String]) {

  val config = ConfigFactory.load(args.head)

  private def getOrElse[T](path: String, c: Config => String => T, default: T): T = {
    if(config.hasPath(path)) {
      c(config)(path)
    } else {
      default
    }
  }

  val isDev = getOrElse("dev", _.getBoolean, false)

  val env = getOrElse("env", _.getString, "dev")
  val secret = getOrElse("secret", _.getString, "changeme")
  val version = getOrElse("version", _.getString, "v1")
  val port = getOrElse("port", _.getInt, 8090)
  val prefix = getOrElse("prefix", _.getString, "api")
  val allowAnonymousUser = getOrElse("allow_anonymous_user", _.getBoolean, false)
  val interface = getOrElse("interface", _.getString, "0.0.0.0")
  val timezone = TimeZone.getDefault
  val db = getOrElse("db.database", _.getString, "ddb")
  val dbServerName = getOrElse("db.serverName", _.getString, "localhost")
  val dbPortNumber = getOrElse("db.portNumber", _.getInt, 5432)
  if (!isDev && secret == "changeme") {
    throw new Exception("CHANGE APPLICATION SECRET")
  }
  val provider = getOrElse("provider", _.getString, "provider")


  val useAws = config.hasPath("aws.accessKeyId") && config.hasPath("aws.secretAccessKey")

  val awsAccess = getOrElse("aws.accessKeyId", _.getString, "")
  val awsSecret = getOrElse("aws.secretAccessKey", _.getString, "")
  val awsBucket = getOrElse("aws.bucket", _.getString, "")

  val emailFrom = getOrElse("aws.emailFrom", _.getString, "bethsaida@pinestreet.org")

  lazy val ds = {
    val internalConfig = {
      import java.io.PrintWriter
      val props = new Properties()
      props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource")
      props.setProperty("dataSource.user", "postgres")
      props.setProperty("dataSource.password", "docker")
      props.setProperty("dataSource.databaseName", db)
      props.setProperty("dataSource.portNumber", dbPortNumber.toString)
      props.setProperty("dataSource.serverName", dbServerName)
      props.put("dataSource.logWriter", new PrintWriter(System.out))
      val config = new HikariConfig(props)
      config.setSchema("bethsaida")
      config
    }

    new HikariDataSource(internalConfig)
  }
}