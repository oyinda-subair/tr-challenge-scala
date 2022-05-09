package com.tr.candlestick.mock

import com.tr.candlestick.controller.CandlestickManager
import com.tr.candlestick.messages.Candlestick
import com.tr.candlestick.models.CandlestickRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MockCandlestickManager(candlestickService: CandlestickRepository) extends CandlestickManager {
  override def getCandlesticks(isin: String): Future[List[Candlestick]] =
    for {
      agg <- candlestickService.fetch(isin)
    } yield agg.toList
}
