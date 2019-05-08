package org.downtowndailybread.bethsaida

import java.util.TimeZone

import com.typesafe.config.Config

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
  val timezone = TimeZone.getDefault
  if (env != "dev" && secret == "changeme") {
    throw new Exception("CHANGE APPLICATION SECRET")
  }
  val provider = getOrElse("provider", _.getString, "provider")
}