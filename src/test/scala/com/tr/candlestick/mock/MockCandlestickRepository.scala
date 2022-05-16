package com.tr.candlestick.mock

import com.tr.candlestick.config.AppConfig.ISIN
import com.tr.candlestick.messages.EventType.{ADD, DELETE}
import com.tr.candlestick.messages._
import com.tr.candlestick.models.CandlestickRepository

import java.time.Instant
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MockCandlestickRepository() extends CandlestickRepository {
  val instrumentCollection: mutable.Map[ISIN, Instrument] = mutable.HashMap[ISIN, Instrument]().empty
  val quoteCollection: mutable.Map[ISIN, mutable.Map[Quote, Instant]] = mutable.HashMap[ISIN, mutable.Map[Quote, Instant]]().empty

  override def saveOrDeleteInstrumentEvent(event: InstrumentEvent): Unit = {
    event.eventType match {
      case ADD => addInstrument(event)
      case DELETE => deleteEvent(event)
    }
  }

  override def saveQuoteEvent(isin: ISIN, event: QuoteEvent): Unit = {
    if(!quoteCollection.contains(isin)) {
      quoteCollection.addOne(isin, mutable.Map(event.data -> Instant.now()))
    } else {
      quoteCollection(isin).put(event.data, Instant.now())
    }

  }

  override def fetchLastThirtyMinutesByIsin(isin: ISIN): Future[Seq[Candlestick]] = {
    if(quoteCollection.contains(isin)) {
      Future{ quoteCollection(isin).map { case (k,v) => Candlestick(v, k.price, k.price, k.price, k.price, v)}.toSeq }
    } else Future.successful(Seq.empty)
  }

  private def addInstrument(event: InstrumentEvent): Unit = {
    instrumentCollection.addOne(event.data.isin, event.data)
  }

  private def deleteEvent(event: InstrumentEvent): Unit = {
    instrumentCollection.remove(event.data.isin)
  }

  override def fetchAllByIsin(isin: ISIN): Future[Seq[Candlestick]] = Future.successful(Seq.empty)
}
