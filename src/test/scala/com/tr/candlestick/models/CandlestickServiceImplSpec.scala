package com.tr.candlestick.models

import com.tr.candlestick.messages.EventType
import com.tr.candlestick.testkit.CandlestickTestkit
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.scalatest.concurrent.ScalaFutures

class CandlestickServiceImplSpec extends AnyWordSpec with Matchers with CandlestickTestkit with ScalaFutures {

  "Add Events CandlestickServiceImpl" when {
    val candlestickRepository = new CandlestickRepositoryImpl(collections)
    val isin = getIsin
    eventInstrumentStream(isin, EventType.ADD, {event =>
      candlestickRepository.addOrDeleteInstrument(event)
      println(event)
    })

    eventQuoteStream(isin, {event =>
      candlestickRepository.addQuote(isin, event)
      println(event)
    })

    "Instrument event" should {
      "exist in database" in {

        val result = candlestickRepository.instrumentCollection.find(org.mongodb.scala.model.Filters.equal("isin", isin)).toFuture()

        whenReady(result) { s =>
          s.isEmpty shouldBe false
        }
      }
    }

    "Quote Event" should {
      "add new quote event" in {
        val result = candlestickRepository.fetch(isin)

        whenReady(result) { s =>
          s.head.openTimestamp isBefore s.head.closeTimestamp
        }
      }
    }
  }

  "Delete Event" when {
    val candlestickRepository = new CandlestickRepositoryImpl(collections)
    val isin = getIsin
    eventInstrumentStream(isin, EventType.ADD, {event =>
      candlestickRepository.addOrDeleteInstrument(event)
      println(event)
    })

    eventQuoteStream(isin, {event =>
      candlestickRepository.addQuote(isin, event)
      println(event)
    })

    eventInstrumentStream(isin, EventType.DELETE, {event =>
      candlestickRepository.addOrDeleteInstrument(event)
      println(event)
    })

    "Instrument event" should {
      "exist in database" in {

        val result = candlestickRepository.instrumentCollection.find(org.mongodb.scala.model.Filters.equal("isin", isin)).toFuture()

        whenReady(result) { s =>
          s.isEmpty shouldBe true
        }
      }
    }

    "Quote Event" should {
      "add new quote event" in {
        val result = candlestickRepository.fetch(isin)

        whenReady(result) { s =>
          println(s)
          s.isEmpty shouldBe true
        }
      }
    }
  }
}
