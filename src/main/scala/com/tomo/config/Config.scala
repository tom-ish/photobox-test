package com.tomo.config

import com.typesafe.config.ConfigFactory

trait Config {

  val config = ConfigFactory.load
  val serverConfig = config.getConfig("server")

}
