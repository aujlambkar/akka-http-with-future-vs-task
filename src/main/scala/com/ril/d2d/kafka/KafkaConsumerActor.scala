package com.ril.d2d.kafka

import java.time.Duration
import java.util
import java.util.Properties

import akka.actor.{ Actor, ActorRef, Props }
import com.ril.d2d.kafka.KafkaConsumerActor.Consume
import com.ril.d2d.workorder.{ WorkOrderJsonSupport, WorkOrders }
import org.apache.kafka.clients.consumer.{ ConsumerConfig, ConsumerRecord, KafkaConsumer }
import org.apache.kafka.common.TopicPartition
import spray.json._

import scala.collection.JavaConversions._

object KafkaConsumerActor {

  def props(broker: String, topic: String, group: String) = Props(new KafkaConsumerActor(broker, topic, group))

  final case class Consume(eventName: String, correlationId: String, callbackActor: ActorRef)
}

class KafkaConsumerActor(broker: String, topic: String, group: String) extends Actor with KafkaJsonSupport with WorkOrderJsonSupport {

  override def receive: Receive = {
    case Consume(eventName, correlationId, callbackActor) => consumeEvent(eventName, correlationId, callbackActor)
  }

  def createKafkaConsumer:KafkaConsumer[String, String] = {
    val kafkaConsumer = new KafkaConsumer[String, String](consumerConfig)
    val partition = new TopicPartition("response", 0)

    kafkaConsumer.subscribe(Seq(topic))
    kafkaConsumer
  }

  def consumeEvent(eventName: String, correlationId: String, callbackActor: ActorRef): Unit = {
    val kafkaConsumer = createKafkaConsumer
    val partition = new TopicPartition("response", 0)
    val consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000L))
    val records = consumerRecords.records(partition)
    println(" $$$$$ KafkaConsumerActor received event :" + records.length)
    records.forEach(record => {
      val event = record.value().parseJson.convertTo[Event]
      println(" $$$$$ KafkaConsumerActor received event :" + event.payload.getOrElse("{}"))
      callbackActor ! event.payload.map(_.parseJson.convertTo[WorkOrders]).getOrElse(WorkOrders(Seq()))
    })

    kafkaConsumer.close
  }

  def consumerConfig: Properties = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, group)
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props
  }
}
