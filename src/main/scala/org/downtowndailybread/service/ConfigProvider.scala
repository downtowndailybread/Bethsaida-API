package org.downtowndailybread.service

import com.typesafe.config.Config

import scala.concurrent.Promise

class ConfigProvider {
  val config = Promise[Config]
}
