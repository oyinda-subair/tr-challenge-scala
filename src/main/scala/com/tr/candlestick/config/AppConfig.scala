package com.tr.candlestick.config

import org.mongodb.scala.{Document, MongoCollection}

object AppConfig {
  type ISIN = String
  type Price = Double
  type InstrumentCollection = MongoCollection[Document]
  type QuoteCollection = MongoCollection[Document]
}
