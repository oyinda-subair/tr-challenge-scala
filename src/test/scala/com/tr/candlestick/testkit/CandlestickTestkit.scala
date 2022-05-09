package com.tr.candlestick.testkit

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.stream.Materializer
import com.tr.candlestick.controller.CandlestickManager
import com.tr.candlestick.messages._
import com.tr.candlestick.mock.{MockCandlestickManager, MockCandlestickRepository}
import com.tr.candlestick.models.CandlestickRepository
import com.tr.candlestick.routes.CandleStickRoute
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._

import scala.concurrent.ExecutionContext

trait CandlestickTestkit extends PlayJsonSupport {
//  implicit val system: ActorSystem = ActorSystem("messaging-actorsystem-test")
//  implicit val materializer: Materializer = Materializer(system)
//  implicit val executionContext: ExecutionContext = system.dispatcher

  val candlestickServiceImpl: CandlestickRepository = new MockCandlestickRepository
  val candlestickManagerImpl: CandlestickManager = new MockCandlestickManager(candlestickServiceImpl)

  lazy val routes = new CandleStickRoute(candlestickManagerImpl).routes

  def toEntity[T: Reads: Writes](body: T): HttpEntity.Strict = {
    val message = Json.toJson(body).toString()
    HttpEntity(MediaTypes.`application/json`, message)
  }
}
