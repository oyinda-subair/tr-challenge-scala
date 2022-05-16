package com.tr.candlestick.models

import com.tr.candlestick.config.AppConfig.InstrumentCollection
import com.tr.candlestick.messages.EventType
import com.tr.candlestick.testkit.CandlestickTestkit
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.mongodb.scala._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import play.api.libs.json._
import org.scalatest.time.{Millis, Seconds, Span}

class CandlestickServiceImplSpec extends AnyWordSpec
  with Matchers
  with CandlestickTestkit
  with ScalaFutures
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with Eventually {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  val instrumentCollection: InstrumentCollection = factory.Instrument(db)

  "Add Events CandlestickServiceImpl" when {

    "Instrument event" should {
      "exist in database" in {

        whenReady(instrumentCollection.find(org.mongodb.scala.model.Filters.equal("isin", isin)).toFuture()) { s =>
          s.isEmpty shouldBe false
          s.length shouldEqual 1
          s.map {doc =>
            val parseJsonString = Json.parse(doc.toJson())

            (parseJsonString \ "isin").as[String] should be(isin)
          }
        }
      }
    }

    "Quote Event" should {
      "add new quote event" in {

        whenReady(candlestickRepository.fetchLastThirtyMinutesByIsin(isin)) { candlesticks =>
          candlesticks.head.openTimestamp isBefore candlesticks.head.closeTimestamp
        }
      }
    }
  }

  "Delete Event" when {

    "Instrument event" should {
      "not exist in database" in {
        eventInstrumentStream(isin, EventType.DELETE, {event =>
          candlestickRepository.saveOrDeleteInstrumentEvent(event)
        })

        eventually {

          whenReady(instrumentCollection.find(org.mongodb.scala.model.Filters.equal("isin", isin)).toFuture()) { s =>
            s.isEmpty shouldBe true
          }
        }
      }
    }

    "Quote Event" should {
      "not exist in database" in {
        eventInstrumentStream(isin, EventType.DELETE, {event =>
          candlestickRepository.saveOrDeleteInstrumentEvent(event)
        })

        eventually {
          whenReady(candlestickRepository.fetchLastThirtyMinutesByIsin(isin)) { s =>
            s.isEmpty shouldBe true
          }
        }
      }
    }
  }
}
