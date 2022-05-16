package com.tr.candlestick.models

import com.tr.candlestick.ServerMain.system.log
import com.tr.candlestick.config.AppConfig.{ISIN, InstrumentCollection, QuoteCollection}
import com.tr.candlestick.config.errorHandler.{InternalServerException, JsonValidationException}
import com.tr.candlestick.database.MongoFactory
import com.tr.candlestick.messages.EventType._
import com.tr.candlestick.messages._
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.model.Filters._
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.time._

class CandlestickRepositoryImpl(instrumentCollection: InstrumentCollection, quoteCollection: QuoteCollection) extends CandlestickRepository {
//  val instrumentCollection: MongoCollection[Document] = mongoFactory.Instrument.collection
//  val quoteCollection: MongoCollection[Document] = mongoFactory.Quote.collection

  val GROUP: org.bson.Document = org.bson.Document.parse("{  $group: {_id: {$add: [{ $subtract: [{ $subtract: [ '$timestamp', new Date(0) ] },{ $mod: [{ $subtract: [ '$timestamp', new Date(0) ] },  { $multiply: [1000, 60, 1] }]}]}, new Date(0)]}, open_timestamp: {$first: '$timestamp'}, open_price: {$first: '$price'}, close_price: {$last: '$price'}, high_price: {$max: '$price'}, low: {$min: '$price'}, close_timestamp: {$last: '$timestamp'} }}")
  val PROJECT: org.bson.Document = org.bson.Document.parse("{  $project: {_id: 1, openPrice: '$open_price', closePrice: '$close_price', highPrice: '$high_price', lowPrice: '$low', openTimestamp: '$open_timestamp', closeTimestamp: '$close_timestamp' }}")
  val SORT: org.bson.Document = org.bson.Document.parse("{  $sort: {    _id: 1  }}")

  override def saveOrDeleteInstrumentEvent(event: InstrumentEvent): Unit = {
    event.eventType match {
      case ADD => saveInstrumentEvent(event)
      case DELETE => deleteEvent(event)
    }
  }

  override def saveQuoteEvent(isin: ISIN, event: QuoteEvent): Unit = {

    if(event.eventType == EventType.QUOTE) {
      val data = event.data

      val doc: Document = Document("isin" -> isin, "price" -> data.price, "type" -> "quote", "timestamp" -> BsonDateTime(Instant.now().toEpochMilli))

      for {
        _ <- quoteCollection.insertOne(doc).toFuture()
      } yield ()
    }

    try {

    } catch {
      case exception: MongoException =>
        log.error(s"SaveQuoteEvent: Unable to save instrument event with isin $isin")
        throw InternalServerException(exception.getMessage)
      case exception: Exception =>
        log.error(s"SaveQuoteEvent Exception: Unable to save instrument event with isin $isin")
        throw exception
    }
  }

  override def fetchLastThirtyMinutesByIsin(isin: ISIN): Future[Seq[Candlestick]] = {

    // last 30
    val expr = org.bson.Document.parse("{ $gt: ['$timestamp', { $dateSubtract: { startDate: new Date(), unit: 'minute', amount: 30 } }  ]}}")
    val doc = new org.bson.Document().append("isin", isin).append("$expr", expr)
    val matchParse = new org.bson.Document().append("$match", doc)

    val query = quoteCollection.aggregate(List(matchParse, GROUP, PROJECT, SORT))
    getFullResult(query)
  }

  override def fetchAllByIsin(isin: ISIN): Future[Seq[Candlestick]] = {
    val docAll = new org.bson.Document().append("isin", isin)
    val matchParseAll = new org.bson.Document().append("$match", docAll)

    val allQuoteQuery = quoteCollection.aggregate(List(matchParseAll, GROUP, PROJECT, SORT))
    getFullResult(allQuoteQuery)
  }

  private def saveInstrumentEvent(event: InstrumentEvent): Unit = {
    val isin = event.data.isin
    val description = event.data.description
    val doc: Document = Document("isin" -> isin, "description" -> description, "type" -> "instrument")

    try {
      instrumentCollection.insertOne(doc).toFuture().map(_ => ())
    } catch {
      case exception: MongoException =>
        log.error(s"SaveInstrumentEvent: Unable to save instrument event with isin $isin")
        throw InternalServerException(exception.getMessage)
      case exception: Exception =>
        log.error(s"SaveInstrumentEvent Exception: Unable to save instrument event with isin $isin")
        throw exception
    }
  }

  private def deleteEvent(event: InstrumentEvent): Future[Unit] = {

    try {
      val quoteQuery = quoteCollection.deleteMany(equal("isin", event.data.isin)).toFuture()
      val instrumentQuery = instrumentCollection.deleteOne(equal("isin", event.data.isin)).toFuture()

      quoteQuery.flatMap(_ => instrumentQuery.map(_ => ()))
    } catch {
      case exception: MongoException =>
        log.error(s"DeleteEvent Exception: Unable to delete events with isin ${event.data.isin}")
        throw InternalServerException(exception.getMessage)
      case exception: Exception =>
        log.error(s"DeleteEvent Exception: Unable to delete events with isin ${event.data.isin}")
        throw exception
    }
  }

  private def getFullResult(query: AggregateObservable[Document]): Future[Seq[Candlestick]] = {

    try {
      query.toFuture().map(_.map(parseFetchResult))
    } catch {
      case exception: MongoException => throw InternalServerException(exception.getMessage)
      case exception: Exception => throw exception
    }
  }

  private def parseFetchResult(document: Document): Candlestick = {
    try {
      val documentJson = document.toJson()
      val parseJsonString = Json.parse(documentJson)

      val openTimestamp = (parseJsonString \ "openTimestamp" \ "$date").as[Instant]
      val closeTimestamp = (parseJsonString \ "closeTimestamp" \ "$date").as[Instant]
      val openPrice = (parseJsonString \ "openPrice").as[Double]
      val closePrice = (parseJsonString \ "closePrice").as[Double]
      val highPrice = (parseJsonString \ "highPrice").as[Double]
      val lowPrice = (parseJsonString \ "lowPrice").as[Double]

      Candlestick(openTimestamp, openPrice, highPrice, lowPrice, closePrice, closeTimestamp)
    } catch {
      case exception: JsResultException => throw JsonValidationException(exception.getMessage)
      case exception: MongoException => throw InternalServerException(exception.getMessage)
      case exception: Exception => throw exception
    }
  }
}
