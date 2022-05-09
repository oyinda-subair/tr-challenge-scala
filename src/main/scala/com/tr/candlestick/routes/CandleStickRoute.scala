package com.tr.candlestick.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.tr.candlestick.controller.CandlestickManager
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

class CandleStickRoute(candlestickManager: CandlestickManager) extends PlayJsonSupport {

  protected val hello: Route =
    path("hello") {
      get {
        complete("Welcome to messaging service")
      }
    }

//  /candlesticks?isin={ISIN}
  protected val getCandlestick: Route =
    path("candlesticks"){
      get {
        parameter("isin") { isin =>
          complete(StatusCodes.OK, candlestickManager.getCandlesticks(isin))
        }
      }
    }

  val routes: Route =
    hello ~
      getCandlestick
}
