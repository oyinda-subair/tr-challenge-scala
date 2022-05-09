package com.tr.candlestick.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.tr.candlestick.messages.{Candlestick, EventType, Instrument, InstrumentEvent, Quote, QuoteEvent}
import com.tr.candlestick.testkit.CandlestickTestkit
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CandlestickRouteSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with CandlestickTestkit {

  val isin = "NM3858548401"
  val instrument: Instrument = Instrument(isin, "etiam eius metus integer facilisis velit")

  candlestickServiceImpl.addOrDeleteInstrument(InstrumentEvent(EventType.ADD, instrument))
  candlestickServiceImpl.addQuote(isin, QuoteEvent(EventType.QUOTE, Quote(isin, 576.2581)))
  candlestickServiceImpl.addQuote(isin, QuoteEvent(EventType.QUOTE, Quote(isin, 726.367)))
  candlestickServiceImpl.addQuote(isin, QuoteEvent(EventType.QUOTE, Quote(isin, 469.8936)))
  candlestickServiceImpl.addQuote(isin, QuoteEvent(EventType.QUOTE, Quote(isin, 554.8741)))
  candlestickServiceImpl.addQuote(isin, QuoteEvent(EventType.QUOTE, Quote(isin, 279.0075)))
  candlestickServiceImpl.addQuote(isin, QuoteEvent(EventType.QUOTE, Quote(isin, 652.1205)))
  candlestickServiceImpl.addQuote(isin, QuoteEvent(EventType.QUOTE, Quote(isin, 1072.5556)))
  candlestickServiceImpl.addQuote(isin, QuoteEvent(EventType.QUOTE, Quote(isin, 496.4715)))

  "Candlestick Route" should {
    "get list of candlestick when isin is provided" in {
      Get(s"/candlesticks?isin=$isin") ~> routes ~> check {
        status shouldEqual StatusCodes.OK

        val response = responseAs[List[Candlestick]]

        response.length should be(8)
      }
    }
  }
}
