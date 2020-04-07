package com.ubirch.test

import com.typesafe.config.{ Config, ConfigFactory }

object ConfigBase {

  private val config = new ConfigBase

  def get(): Config = config.conf
}

class ConfigBase {
  lazy val conf: Config = ConfigFactory.load()
}
