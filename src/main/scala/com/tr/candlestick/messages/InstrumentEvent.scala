package com.tr.candlestick.messages

import com.tr.candlestick.messages.EventType._
import play.api.libs.json.{Format, Json}

case class InstrumentEvent(eventType: EventType, data: Instrument) {
  override def toString: String = s"InstrumentEvent(type=$eventType, data=$data)"
}

object InstrumentEvent {
  implicit val format: Format[InstrumentEvent] = Json.format
}
