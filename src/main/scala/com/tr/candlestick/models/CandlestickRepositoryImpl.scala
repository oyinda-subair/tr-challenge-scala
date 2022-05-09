package com.tr.candlestick.models

import akka.util.Collections
import com.tr.candlestick.config.AppConfig.ISIN
import com.tr.candlestick.database.MongoFactory
import com.tr.candlestick.messages.EventType._
import com.tr.candlestick.messages._
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.ObservableImplicits
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.time._
import scala.collection.immutable.{AbstractSeq, LinearSeq}
import scala.util.{Failure, Success}

class CandlestickRepositoryImpl(mongoFactory: MongoFactory) extends CandlestickRepository {
  val instrumentCollection: MongoCollection[Document] = mongoFactory.Instrument.collection
  val quoteCollection: MongoCollection[Document] = mongoFactory.Quote.collection

  val GROUP: org.bson.Document = org.bson.Document.parse("{  $group: {_id: {$add: [{ $subtract: [{ $subtract: [ '$timestamp', new Date(0) ] },{ $mod: [{ $subtract: [ '$timestamp', new Date(0) ] },  { $multiply: [1000, 60, 1] }]}]}, new Date(0)]}, open_timestamp: {$first: '$timestamp'}, open_price: {$first: '$price'}, close_price: {$last: '$price'}, high_price: {$max: '$price'}, low: {$min: '$price'}, close_timestamp: {$last: '$timestamp'} }}")
  val PROJECT: org.bson.Document = org.bson.Document.parse("{  $project: {_id: 1, openPrice: '$open_price', closePrice: '$close_price', highPrice: '$high_price', lowPrice: '$low', openTimestamp: '$open_timestamp', closeTimestamp: '$close_timestamp' }}")
  val SORT: org.bson.Document = org.bson.Document.parse("{  $sort: {    _id: 1  }}")

  override def addOrDeleteInstrument(event: InstrumentEvent): Unit = {
    event.eventType match {
      case ADD => addInstrument(event)
      case DELETE => deleteEvent(event)
    }
  }

  override def addQuote(isin: ISIN, event: QuoteEvent): Unit = {

    if(event.eventType == EventType.QUOTE) {
      val data = event.data

      val doc: Document = Document("isin" -> isin, "price" -> data.price, "type" -> "quote", "timestamp" -> BsonDateTime(Instant.now().toEpochMilli))

      for {
        _ <- quoteCollection.insertOne(doc).toFuture()
      } yield ()
    }
  }

  override def fetch(isin: ISIN): Future[Seq[Candlestick]] = {

    // last 30
    val expr = org.bson.Document.parse("{ $gt: ['$timestamp', { $dateSubtract: { startDate: new Date(), unit: 'minute', amount: 30 } }  ]}}")
    val doc = new org.bson.Document().append("isin", isin).append("$expr", expr)
    val matchParse = new org.bson.Document().append("$match", doc)

    val query = quoteCollection.aggregate(List(matchParse, GROUP, PROJECT, SORT))
    getFullResult(query)
  }

  override def fetchAll(isin: ISIN): Future[Seq[Candlestick]] = {
    val docAll = new org.bson.Document().append("isin", isin)
    val matchParseAll = new org.bson.Document().append("$match", docAll)

    val allQuoteQuery = quoteCollection.aggregate(List(matchParseAll, GROUP, PROJECT, SORT))
    getFullResult(allQuoteQuery)
  }

  private def addInstrument(event: InstrumentEvent): Unit = {
    val isin = event.data.isin
    val description = event.data.description
    val doc: Document = Document("isin" -> isin, "description" -> description, "type" -> "instrument")

    for {
      _ <- instrumentCollection.insertOne(doc).toFuture()
    } yield ()
  }

  private def deleteEvent(event: InstrumentEvent) = {
    val quoteQuery = quoteCollection.deleteMany(equal("isin", event.data.isin)).toFuture()
    val instrumentQuery = instrumentCollection.deleteOne(equal("isin", event.data.isin)).toFuture()

    for {
      _ <- quoteQuery
      _ <- instrumentQuery
    } yield ()
  }

  private def getFullResult(query: AggregateObservable[Document]): Future[Seq[Candlestick]] = {
    for {
      result <- query.toFuture()
      parsed = result.map(parseFetchResult)
    } yield parsed
  }

  private def parseFetchResult(document: Document): Candlestick = {
    val documentJson = document.toJson()
    val parseJsonString = Json.parse(documentJson)

    val openTimestamp = (parseJsonString \ "openTimestamp" \ "$date").as[Instant]
    val closeTimestamp = (parseJsonString \ "closeTimestamp" \ "$date").as[Instant]
    val openPrice = (parseJsonString \ "openPrice").as[Double]
    val closePrice = (parseJsonString \ "closePrice").as[Double]
    val highPrice = (parseJsonString \ "highPrice").as[Double]
    val lowPrice = (parseJsonString \ "lowPrice").as[Double]

    Candlestick(openTimestamp, openPrice, highPrice, lowPrice, closePrice, closeTimestamp)
  }
}
