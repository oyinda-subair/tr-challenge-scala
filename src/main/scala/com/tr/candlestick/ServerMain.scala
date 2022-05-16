package com.tr.candlestick

import ch.qos.logback.classic.{Level, Logger}
import com.tr.candlestick.config.base.ServiceConfig
import com.tr.candlestick.controller.{CandlestickManager, CandlestickManagerImpl}
import com.tr.candlestick.database.MongoFactory
import com.tr.candlestick.models.{CandlestickRepository, CandlestickRepositoryImpl}
import com.tr.candlestick.partner.stream.{InstrumentStream, QuoteStream}
import com.tr.candlestick.routes.CandleStickRoute
import org.mongodb.scala.MongoDatabase
import org.slf4j.LoggerFactory

object ServerMain extends ServiceConfig {
  LoggerFactory.getLogger("org.mongodb.driver").asInstanceOf[Logger].setLevel(Level.ERROR)

  val candlestickServiceImpl: CandlestickRepository = {
    val db: MongoDatabase = MongoFactory.candlestickDatabase
    val factory = new MongoFactory()
    new CandlestickRepositoryImpl(factory.Instrument(db), factory.Quote(db))
  }
  val candlestickManagerImpl: CandlestickManager = new CandlestickManagerImpl(candlestickServiceImpl)

  val instrumentStream: InstrumentStream = new InstrumentStream()
  val quoteStream: QuoteStream = new QuoteStream()

  instrumentStream.connect(event => {
    candlestickServiceImpl.saveOrDeleteInstrumentEvent(event)
    println(event)
  })

  quoteStream.connect(event => {
    candlestickServiceImpl.saveQuoteEvent(event.data.isin, event)
    println(event)
  })

  override val routes = new CandleStickRoute(candlestickManagerImpl).routes

  startService()
}
