package com.tr.candlestick.messages

import com.tr.candlestick.config.AppConfig.ISIN
import play.api.libs.json.{Format, Json}

case class Instrument(isin: ISIN, description: String){
  override def toString: ISIN = s"Instrument(isin=$isin, description=$description)"
}

object Instrument {
  implicit val format: Format[Instrument] = Json.format
}