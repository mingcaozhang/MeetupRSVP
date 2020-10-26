package com.zhangmin.meetup.config

import pureconfig.error.ConfigReaderFailures
import pureconfig.{ConfigReader, ConfigSource}

object SettingsLoader {
  def load[T: ConfigReader](applicationName: String): T = ConfigSource.default.at(applicationName).load[T] match {
    case Left(_: ConfigReaderFailures) =>
      sys.exit(1)
    case Right(conf: T) => conf
  }
}
