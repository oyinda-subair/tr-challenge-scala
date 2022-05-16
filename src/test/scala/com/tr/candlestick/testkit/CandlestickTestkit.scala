package com.tr.candlestick.testkit

import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.server.Route
import ch.qos.logback.classic.{Level, Logger}
import com.tr.candlestick.config.AppConfig.ISIN
import com.tr.candlestick.controller.CandlestickManager
import com.tr.candlestick.database.MongoFactory
import com.tr.candlestick.messages.EventType._
import com.tr.candlestick.messages._
import com.tr.candlestick.mock.{MockCandlestickManager, MockCandlestickRepository}
import com.tr.candlestick.models.{CandlestickRepository, CandlestickRepositoryImpl}
import com.tr.candlestick.routes.CandleStickRoute
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.mongodb.scala.MongoDatabase
import org.slf4j.LoggerFactory
import play.api.libs.json._

import scala.util.Random

trait CandlestickTestkit extends PlayJsonSupport {
  LoggerFactory.getLogger("org.mongodb.driver").asInstanceOf[Logger].setLevel(Level.ERROR)

  val isin: ISIN = Random.alphanumeric.filter(_.isLetter).take(16).mkString

  val mockCandlestickServiceImpl: CandlestickRepository = new MockCandlestickRepository()
  val db: MongoDatabase = MongoFactory.candlestickDatabase
  val factory = new MongoFactory()
  val candlestickRepository: CandlestickRepository = new CandlestickRepositoryImpl(factory.Instrument(db), factory.Quote(db))

  eventInstrumentStream(isin, EventType.ADD, {event =>
    candlestickRepository.saveOrDeleteInstrumentEvent(event)
  })

  eventQuoteStream(isin, {event =>
    candlestickRepository.saveQuoteEvent(isin, event)
  })

  lazy val routes: Route = {
    val mockCandlestickManagerImpl: CandlestickManager = new MockCandlestickManager(mockCandlestickServiceImpl)
    new CandleStickRoute(mockCandlestickManagerImpl).routes
  }

  def toEntity[T: Reads: Writes](body: T): HttpEntity.Strict = {
    val message = Json.toJson(body).toString()
    HttpEntity(MediaTypes.`application/json`, message)
  }

  def eventInstrumentStream(isin: ISIN, eventType: EventType, toEvent: InstrumentEvent => Unit): Unit = {
    val instrument: Instrument = Instrument(isin, "etiam eius metus integer facilisis velit")
    toEvent(InstrumentEvent(eventType, instrument))
  }

  def eventQuoteStream(isin: ISIN, toEvent: QuoteEvent => Unit): LazyList[Unit] = {
    val r = new Random(100)
    val price =  for (i <- 0 to r.nextInt(100)) yield r.nextDouble() + r.nextFloat()
    price.map( p => toEvent(QuoteEvent(EventType.QUOTE, Quote(isin, p)))).to(LazyList)
  }
}
