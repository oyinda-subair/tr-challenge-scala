package com.tr.candlestick.config.errorHandler

import akka.http.scaladsl.server.Rejection

case class UnauthorizedUser(message: String) extends Rejection
