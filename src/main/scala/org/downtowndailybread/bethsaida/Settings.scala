package org.downtowndailybread.bethsaida

import java.util.{Properties, TimeZone}

import com.typesafe.config.Config
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

class Settings(config: Config) {

  private def getOrElse[T](path: String, c: Config => String => T, default: T): T = {
    if(config.hasPath(path)) {
      c(config)(path)
    } else {
      default
    }
  }

  val env = getOrElse("env", _.getString, "dev")
  val secret = getOrElse("secret", _.getString, "changeme")
  val version = getOrElse("version", _.getString, "v1")
  val port = getOrElse("port", _.getInt, 8090)
  val prefix = getOrElse("prefix", _.getString, "api")
  val allowAnonymousUser = getOrElse("allow_anonymous_user", _.getBoolean, false)
  val interface = getOrElse("interface", _.getString, "0.0.0.0")
  val timezone = TimeZone.getDefault
  val db = getOrElse("database", _.getString, "ddb")
  if (env != "dev" && secret == "changeme") {
    throw new Exception("CHANGE APPLICATION SECRET")
  }
  val provider = getOrElse("provider", _.getString, "provider")

  lazy val ds = {
    val internalConfig = {
      import java.io.PrintWriter
      val props = new Properties()
      props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource")
      props.setProperty("dataSource.user", "postgres")
      props.setProperty("dataSource.password", "docker")
      props.setProperty("dataSource.databaseName", db)
      props.put("dataSource.logWriter", new PrintWriter(System.out))
      val config = new HikariConfig(props)
      config.setSchema("bethsaida")
      config
    }

    new HikariDataSource(internalConfig)
  }
}