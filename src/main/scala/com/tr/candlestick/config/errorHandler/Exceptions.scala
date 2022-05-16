package com.tr.candlestick.config.errorHandler

case class ResourceNotFoundException(message: String, cause: Option[Throwable] = None)
    extends Exception(message, cause.orNull)

case class InternalServerException(message: String, cause: Option[Throwable] = None)
    extends Exception(message, cause.orNull)

case class JsonValidationException(message: String, cause: Option[Throwable] = None)
    extends Exception(message, cause.orNull)

case class BadRequestException(message: String, cause: Option[Throwable] = None)
  extends Exception(message, cause.orNull)