package com.tr.candlestick

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.tr.candlestick.controller.{CandlestickManager, CandlestickManagerImpl}
import com.tr.candlestick.database.MongoFactory
import com.tr.candlestick.models.{CandlestickRepository, CandlestickRepositoryImpl}
import com.tr.candlestick.partner.stream.{InstrumentStream, QuoteStream}
import com.tr.candlestick.routes.CandleStickRoute

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object ServerMain extends App {
  implicit val system: ActorSystem = ActorSystem("messaging-actorsystem")
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext: ExecutionContext = system.dispatcher

  val collections = new MongoFactory()

  val candlestickServiceImpl: CandlestickRepository = new CandlestickRepositoryImpl(collections)
  val instrumentStream: InstrumentStream = new InstrumentStream()
  val quoteStream: QuoteStream = new QuoteStream()
  val candlestickManagerImpl: CandlestickManager = new CandlestickManagerImpl(candlestickServiceImpl)
  def helloRoute: Route = new CandleStickRoute(candlestickManagerImpl).routes

  instrumentStream.connect(event => {
    candlestickServiceImpl.addOrDeleteInstrument(event)
    println(event)
  })

  quoteStream.connect(event => {
    candlestickServiceImpl.addQuote(event.data.isin, event)
    println(event)
  })

  // bind the route using HTTP to the server address and port
  val bindingFuture = Http().newServerAt("localhost", 9000).bind(helloRoute)
  println("Server running...")

  // kill the server with input
  println(s"Server now online. Please navigate to http://localhost:9000/hello\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  println("Server is shut down")


}
