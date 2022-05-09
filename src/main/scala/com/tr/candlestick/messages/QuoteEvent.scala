package com.tr.candlestick.messages

import com.tr.candlestick.messages.EventType.EventType
import play.api.libs.json.{Format, Json}

case class QuoteEvent(eventType: EventType, data: Quote) {
  override def toString: String = s"QuoteEvent(tyep=$eventType, data=$data)"
}

object QuoteEvent {
  implicit val format: Format[QuoteEvent] = Json.format
}