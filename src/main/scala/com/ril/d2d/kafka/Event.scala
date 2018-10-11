package com.ril.d2d.kafka

case class Event(name: String, timestamp: Long, correlationId: String, payload: Option[String])
