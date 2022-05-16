package com.tr.candlestick.database

import com.tr.candlestick.config.AppConfig.{InstrumentCollection, QuoteCollection}
import com.typesafe.config.ConfigFactory
import org.mongodb.scala._

object MongoFactory {

  private def getMongoClientSettings: MongoClientSettings = {
    val uri = "mongodb://root:example@0.0.0.0:27017/?maxPoolSize=20&w=majority"
    val mongoClientSettings = MongoClientSettings.builder().applyConnectionString(ConnectionString(uri)).writeConcern(WriteConcern.MAJORITY).build()
    mongoClientSettings
  }
  private val mongoClient = MongoClient(getMongoClientSettings)

  private val config = ConfigFactory.load()
  private val app = config.getConfig("app")

  private val databaseString: String = app.getString("database")
  private val database: MongoDatabase = mongoClient.getDatabase(databaseString)

  def candlestickDatabase: MongoDatabase = database

}

class MongoFactory {
  object Instrument {
    def apply(db: MongoDatabase): InstrumentCollection = db.getCollection("instrument_events")
  }

  object Quote {
    def apply(db: MongoDatabase): QuoteCollection = db.getCollection("quote_events")
  }
}