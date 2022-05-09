package com.tr.candlestick.testkit

import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.server.Route
import com.tr.candlestick.config.AppConfig.ISIN
import com.tr.candlestick.controller.CandlestickManager
import com.tr.candlestick.database.MongoFactory
import com.tr.candlestick.messages.EventType._
import com.tr.candlestick.messages._
import com.tr.candlestick.mock.{MockCandlestickManager, MockCandlestickRepository}
import com.tr.candlestick.models.{CandlestickRepository, CandlestickRepositoryImpl}
import com.tr.candlestick.routes.CandleStickRoute
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._

import scala.util.Random

trait CandlestickTestkit extends PlayJsonSupport {

  val collections = new MongoFactory()

  val mockCandlestickServiceImpl: CandlestickRepository = new MockCandlestickRepository(collections)
  val mockCandlestickManagerImpl: CandlestickManager = new MockCandlestickManager(mockCandlestickServiceImpl)

  def getIsin: String = Random.alphanumeric.filter(_.isLetter).take(16).mkString

//  eventQuoteStream(isin, { event =>
//    val instrument = eventInstrumentStream(isin)
//    mockCandlestickServiceImpl.addQuote(isin, event)
//
//    val candlestickRepository = new CandlestickRepositoryImpl(collections)
//    candlestickRepository.addQuote(isin, event)
//    println(event)
//  })

  lazy val routes: Route = new CandleStickRoute(mockCandlestickManagerImpl).routes

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
