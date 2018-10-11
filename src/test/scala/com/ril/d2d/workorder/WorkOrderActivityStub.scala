package com.ril.d2d.workorder

import java.time.{Duration, LocalDateTime, ZoneOffset}
import java.util
import java.util.Properties

import com.ril.d2d.kafka.{Event, KafkaJsonSupport}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.TopicPartition

import scala.collection.JavaConversions._
import spray.json._

object WorkOrderActivityStub extends App with KafkaJsonSupport with WorkOrderJsonSupport {
  val broker = "100.96.8.53:9092"

  val producer = new KafkaProducer[String, String](producerConfig(broker))
  val consumer = new KafkaConsumer[String, String](consumerConfig(broker))

  val partition = new TopicPartition("request", 0)
  consumer.assign(Seq(partition))

  val offsetMetadata = Option(consumer.committed(partition))
  offsetMetadata match {
    case Some(metadata) => consumer.seek(partition, metadata.offset())
    case None => consumer.seek(partition, 0)
  }


  while (true) {
    val consumerRecords: ConsumerRecords[String, String] = consumer.poll(Duration.ofMillis(1000L))

    val records = consumerRecords.records(partition)

    records.forEach(record => {
      val message = record.value()
      println("***** Got message :" + message)

      val incomingEvent = message.parseJson.convertTo[Event]
      var workOrders = WorkOrders(Seq(WorkOrder(12345, "Fiber Installation Only", LocalDateTime.now().toString(), "Navi Mumbai", "Assigned")))
      val outgoingEvent = Event("GetWorkOrders", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), incomingEvent.correlationId,
        Some(workOrders.toJson.compactPrint))

      val data: ProducerRecord[String, String] = new ProducerRecord[String, String]("response", outgoingEvent.toJson.compactPrint)

      println("##### Producing Event:" + outgoingEvent)
      producer.send(data)
      println("##### Event produced")

      Thread.sleep(1000)
    })
  }


  def consumerConfig(broker: String): Properties = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "1")
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props
  }

  def producerConfig(broker: String): Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", broker)
    props.put("client.id", "ScalaProducerExample")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props
  }

}
