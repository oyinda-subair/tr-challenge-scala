package com.tr.candlestick.partner.stream

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws._
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.fasterxml.jackson.databind.ObjectMapper
import com.tr.candlestick.messages.EventType.EventType
import com.tr.candlestick.messages.{Quote, QuoteEvent}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

class QuoteStream(implicit system: ActorSystem) {
  val objectMapper = new ObjectMapper()

  def connect(onEvent: QuoteEvent => Unit):Unit = {
    val incoming: Sink[Message, Future[Done]] =
      Flow[Message].mapAsync(4) {
        case message: TextMessage.Strict =>
          val quoteEvent = parseEventTextMessage(message)
          onEvent(quoteEvent)
          Future.successful(Done)
        case message: TextMessage.Streamed =>
          message.textStream.runForeach(println)
        case message: BinaryMessage =>
          message.dataStream.runWith(Sink.ignore)
      }.toMat(Sink.last)(Keep.right)

    val outgoing: Source[Message, Promise[Option[Message]]] = Source.maybe[Message]

    val request: WebSocketRequest = WebSocketRequest("ws://localhost:8032/quotes")

    val webSocketFlow: Flow[Message, Message, Future[WebSocketUpgradeResponse]] = Http().webSocketClientFlow(request)

    val ((_, upgradeResponse), closed) =
      outgoing
        .viaMat(webSocketFlow)(Keep.both)
        .toMat(incoming)(Keep.both)
        .run()

    val connected: Future[Done.type] = upgradeResponse.flatMap { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Future.successful(Done)
      } else {
        throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
      }
    }

    connected.onComplete(println)
    closed.foreach(_ => println("closed"))
  }

  private def parseEventTextMessage(message: TextMessage.Strict): QuoteEvent = {
    val parsed = Json.parse(message.text)
    val eventType =  (parsed \ "type").as[EventType]
    val data = (parsed \ "data").as[Quote]

    QuoteEvent(eventType, data)
  }
}
