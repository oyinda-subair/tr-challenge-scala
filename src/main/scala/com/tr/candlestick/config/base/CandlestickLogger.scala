package com.tr.candlestick.config.base

import org.slf4j._

object CandlestickLogger {
  lazy val className: String =
    if (this.getClass.getCanonicalName != null)
      this.getClass.getCanonicalName
    else "none"

  val logger: org.slf4j.Logger = LoggerFactory.getLogger(className)
}
