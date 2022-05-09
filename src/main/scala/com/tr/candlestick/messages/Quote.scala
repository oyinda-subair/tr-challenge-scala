package com.tr.candlestick.messages

import com.tr.candlestick.config.AppConfig.{ISIN, Price}
import play.api.libs.json.{Format, Json}

case class Quote(isin: ISIN, price: Price) {
  override def toString: String = s"Quote(isin=$isin, price=$price)"
}

object Quote {
  implicit val format: Format[Quote] = Json.format
}