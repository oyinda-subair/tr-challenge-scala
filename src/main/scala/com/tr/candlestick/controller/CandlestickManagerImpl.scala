package com.tr.candlestick.controller

import com.tr.candlestick.messages.Candlestick
import com.tr.candlestick.models.CandlestickRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CandlestickManagerImpl(candlestickService: CandlestickRepository) extends CandlestickManager {

  override def getCandlesticks(isin: String): Future[List[Candlestick]] = {
    candlestickService.fetch(isin).flatMap { s =>
      if(s.isEmpty) candlestickService.fetchAll(isin).map(t => t.toList)
      else Future {
        s.toList
      }
    }
  }
}
