package com.tr.candlestick.models

import com.tr.candlestick.config.AppConfig.ISIN
import com.tr.candlestick.messages._

import scala.concurrent.Future

trait CandlestickRepository {
  def saveOrDeleteInstrumentEvent(event: InstrumentEvent): Unit

  def saveQuoteEvent(isin:ISIN, event: QuoteEvent): Unit

  def fetchLastThirtyMinutesByIsin(isin: ISIN):Future[Seq[Candlestick]]

  def fetchAllByIsin(isin: ISIN):Future[Seq[Candlestick]]
}
