package com.tr.candlestick.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.tr.candlestick.messages._
import com.tr.candlestick.testkit.CandlestickTestkit
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CandlestickRouteSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with CandlestickTestkit {

  "Candlestick Route" should {
    "get list of candlestick when isin is provided" in {
      eventInstrumentStream(isin, EventType.ADD, { event =>
        mockCandlestickServiceImpl.saveOrDeleteInstrumentEvent(event)
      })

      eventQuoteStream(isin, { event =>
        mockCandlestickServiceImpl.saveQuoteEvent(isin, event)
      })

      Get(s"/candlesticks?isin=$isin") ~> routes ~> check {
        status shouldEqual StatusCodes.OK

        val response = responseAs[List[Candlestick]]

        response.isEmpty should be(false)
      }
    }
  }
}
