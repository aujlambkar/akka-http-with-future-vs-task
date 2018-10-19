package com.ril.d2d.kafka

import java.util.Properties

import akka.actor.{ Actor, Props }
import com.ril.d2d.kafka.KafkaProducerActor.Produce
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord }
import spray.json._

object KafkaProducerActor {

  def props(producer: KafkaProducer[String, String], topic: String) = Props(new KafkaProducerActor(producer, topic))

  final case class Produce(event: Event)

  def producerConf(broker: String) = {
    val props = new Properties()
    props.put("bootstrap.servers", broker)
    props.put("client.id", "activity.discovery")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props
  }
}

class KafkaProducerActor(producer: KafkaProducer[String, String], topic: String) extends Actor with KafkaJsonSupport {

  override def receive: Receive = {
    case Produce(event) => publishEvent(event, producer, topic)
  }

  def publishEvent(event: Event, producer: KafkaProducer[String, String], topic: String): Unit = {
    val data: ProducerRecord[String, String] = new ProducerRecord[String, String](topic, event.toJson.compactPrint)
    producer.send(data)
  }

}
