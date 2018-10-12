package com.ril.d2d.kafka

import java.time.Duration
import java.util.Properties

import akka.actor.{Actor, ActorRef, Props}
import com.ril.d2d.kafka.KafkaConsumerActor.StartPolling
import com.ril.d2d.kafka.ResponseHandlerActor.HandleResponse
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import spray.json._

import scala.collection.JavaConversions._

object KafkaConsumerActor {

  def props(broker: String, topic: String, group: String, responseHandler: ActorRef) = Props(new KafkaConsumerActor(broker, topic, group, responseHandler))

  final object StartPolling
}

class KafkaConsumerActor(broker: String, topic: String, group: String, responseHandler: ActorRef) extends Actor with KafkaJsonSupport{

  override def receive: Receive = {
    case StartPolling => pollRecord
  }

  def pollRecord() = {
    val kafkaConsumer = new KafkaConsumer[String, String](consumerConfig)
    kafkaConsumer.subscribe(Seq(topic))
    while(true) {
      val consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000L))
      val partition = new TopicPartition(topic, 0)
      val records = consumerRecords.records(partition)
      println(" $$$$$ KafkaConsumerActor received event :" + records.length)
      records.forEach(record => {
        val event = record.value().parseJson.convertTo[Event]
        println(" $$$$$ KafkaConsumerActor received event :" + event)
        responseHandler ! HandleResponse(event)
      })
    }
  }

  def consumerConfig: Properties = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, group)
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "3000")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props
  }
}
