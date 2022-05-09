package com.tr.candlestick.models

import com.tr.candlestick.config.AppConfig.ISIN
import com.tr.candlestick.messages._

import scala.concurrent.Future

trait CandlestickRepository {
  def addOrDeleteInstrument(event: InstrumentEvent): Unit

  def addQuote(isin:ISIN, event: QuoteEvent): Unit

  def fetch(isin: ISIN):Future[Seq[Candlestick]]

  def fetchAll(isin: ISIN):Future[Seq[Candlestick]]
}
