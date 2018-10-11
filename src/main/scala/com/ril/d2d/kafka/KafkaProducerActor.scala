package com.ril.d2d.kafka

import java.util.Properties

import akka.actor.{ Actor, Props }
import com.ril.d2d.kafka.KafkaProducerActor.Produce
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord }
import spray.json._

object KafkaProducerActor {

  def props(broker: String, topic: String) = Props(new KafkaProducerActor(broker, topic))

  final case class Produce(event: Event)
}

class KafkaProducerActor(broker: String, topic: String) extends Actor with KafkaJsonSupport {

  override def receive: Receive = {
    case Produce(event) => publishEvent(event, broker, topic)
  }

  def publishEvent(event: Event, broker: String, topic: String): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", broker)
    props.put("client.id", "activity.discovery")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    val data: ProducerRecord[String, String] = new ProducerRecord[String, String](topic, event.toJson.compactPrint)
    println(" $$$$$ KafkaProducerActor producing event :" + data)
    producer.send(data)
    println(" $$$$$ KafkaProducerActor producing event : DONE")
  }

}
