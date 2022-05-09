package com.tr.candlestick.messages

import play.api.libs.json._

object EventType extends Enumeration {

  type EventType = Value

  val ADD: EventType.Value = Value("ADD")
  val DELETE: EventType.Value = Value("DELETE")
  val QUOTE: EventType.Value = Value("QUOTE")

  implicit val format: Format[EventType.Value] = new Format[EventType.Value] {
    override def writes(o: EventType.Value): JsValue = Json.toJson(o.toString)
    override def reads(json: JsValue): JsResult[EventType.Value] = json.validate[String].map(EventType.withName)
  }

}


