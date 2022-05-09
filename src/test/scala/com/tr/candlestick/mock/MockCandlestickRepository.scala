package com.tr.candlestick.mock

import com.tr.candlestick.config.AppConfig.ISIN
import com.tr.candlestick.database.MongoFactory
import com.tr.candlestick.messages.EventType.{ADD, DELETE}
import com.tr.candlestick.messages._
import com.tr.candlestick.models.CandlestickRepository
import org.mongodb.scala.{Document, MongoCollection}

import java.time.Instant
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MockCandlestickRepository(mongoFactory: MongoFactory) extends CandlestickRepository {
  val instrument: MongoCollection[Document] = mongoFactory.Instrument.collection
  val instrumentCollection: mutable.Map[ISIN, Instrument] = mutable.HashMap[ISIN, Instrument]().empty
  val quoteCollection: mutable.Map[ISIN, mutable.Map[Quote, Instant]] = mutable.HashMap[ISIN, mutable.Map[Quote, Instant]]().empty

  override def addOrDeleteInstrument(event: InstrumentEvent): Unit = {
    event.eventType match {
      case ADD => addInstrument(event)
      case DELETE => deleteEvent(event)
    }
  }

  override def addQuote(isin: ISIN, event: QuoteEvent): Unit = {
    if(!quoteCollection.contains(isin)) {
      quoteCollection.addOne(isin, mutable.Map(event.data -> Instant.now()))
    } else {
      quoteCollection(isin).put(event.data, Instant.now())
    }

  }

  override def fetch(isin: ISIN): Future[Seq[Candlestick]] = {
    instrument.find().toFuture().map(s => s)
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

  override def fetchAll(isin: ISIN): Future[Seq[Candlestick]] = Future.successful(Seq.empty)
}
