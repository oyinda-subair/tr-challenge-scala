package com.tr.candlestick.database

import org.bson.{BsonDocument, BsonInt64}
import org.bson.conversions.Bson
import org.mongodb.scala._

object MongoFactory {

  private def getMongoClientSettings: MongoClientSettings = {
    val uri = "mongodb://root:example@0.0.0.0:27017/?maxPoolSize=20&w=majority"
    val mongoClientSettings = MongoClientSettings.builder().applyConnectionString(ConnectionString(uri)).writeConcern(WriteConcern.MAJORITY).build()
    mongoClientSettings
  }
  private val mongoClient = MongoClient(getMongoClientSettings)
  private val database: MongoDatabase = mongoClient.getDatabase("candlestick")

  try{
    val command: Bson = new BsonDocument("ping", new BsonInt64(1))
    val commandResult = database.runCommand(command)
    println("Connected successfully to server.")
  } catch {
    case ex: MongoException =>
      System.err.println("An error occurred while attempting to run a command: " + ex)
  }

  val candlestickDatabase: MongoDatabase = database
}

class MongoFactory {
  import com.tr.candlestick.database.MongoFactory.candlestickDatabase

  object Instrument {
    val collection: MongoCollection[Document] = candlestickDatabase.getCollection("instrument_events")
  }

  object Quote {
    val collection: MongoCollection[Document] = candlestickDatabase.getCollection("quote_events")
  }
}