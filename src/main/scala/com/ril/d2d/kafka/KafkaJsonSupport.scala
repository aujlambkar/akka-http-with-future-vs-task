package com.ril.d2d.kafka

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

trait KafkaJsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val eventJsonFormat: RootJsonFormat[Event] = jsonFormat4(Event.apply)

}
