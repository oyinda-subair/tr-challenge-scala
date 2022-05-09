package com.tr.candlestick.controller

import com.tr.candlestick.messages.Candlestick

import scala.concurrent.Future

trait CandlestickManager {
  def getCandlesticks(isin: String): Future[List[Candlestick]]
}
