package com.tr.candlestick.messages

import com.tr.candlestick.config.AppConfig.Price
import com.tr.candlestick.config.FormatEntity
import play.api.libs.json.{Format, Json}

import java.time.Instant

case class Candlestick(
                        openTimestamp: Instant,
                        openPrice: Price,
                        highPrice: Price,
                        lowPrice: Price,
                        closePrice: Price,
                        closeTimestamp: Instant
                      )

object Candlestick extends FormatEntity[Candlestick] {
  implicit val format: Format[Candlestick] = Json.format
}
