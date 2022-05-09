package com.tr.candlestick.config

import play.api.libs.json._

import java.time.Instant

trait FormatEntity[T] {

  implicit val formatDateTime: Format[Instant] = new Format[Instant] {

    def reads(json: JsValue): JsResult[Instant] =
      json match {
        case JsNumber(ms) => JsSuccess(Instant.ofEpochMilli(ms.toLong))
        case JsString(str) =>
          try JsSuccess(Instant.parse(str))
          catch {
            case _: IllegalArgumentException =>
              JsError(
                s"Expected a String representation of a date. Value '$str' does not look like one."
              )
          }
        case _ => JsError("String value expected")
      }

    def writes(d: Instant): JsValue = JsString(d.toString)
  }
  implicit val format: Format[T]
}
