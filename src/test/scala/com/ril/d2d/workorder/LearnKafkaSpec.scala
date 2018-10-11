package com.ril.d2d.workorder

import java.time.Duration
import java.util.Properties

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.TopicPartition
import org.scalatest.{Matchers, _}

import scala.collection.JavaConversions._


class LearnKafkaSpec extends WordSpec with Matchers {

  "Kafka" should {
    val brokers = "100.96.8.53:9092"
    val topic = "request"

    "produce message" in {

      produceMessage(brokers, topic, "Test message 123")

      1 should be(1)
    }

    "consume message" in {
      val props = createConsumerConfig(brokers)
      val consumer = new KafkaConsumer[String, String](props)

      val partition = new TopicPartition("request", 0)

      consumer.assign(Seq(partition))

      val offsetMetadata = consumer.committed(partition)
      val offset = offsetMetadata.offset()

      consumer.seek(partition, offset)

      produceMessage(brokers, "request", "Test message 123")
      Thread.sleep(500)

      val consumerRecords = consumer.poll(Duration.ofMillis(1000L))
      consumer.commitSync()

      val firstRecord = consumerRecords.records(partition).head

      firstRecord.value() should be("Test message 123")
    }
  }


  def produceMessage(brokers: String, topic: String, message: String) {

    val props = new Properties()
    props.put("bootstrap.servers", brokers)
    props.put("client.id", "ScalaProducerExample")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](props)

    val data: ProducerRecord[String, String] = new ProducerRecord[String, String](topic, message)

    println("################### Started Message sent ##############")
    producer.send(data)
    println("################### Competed Message sent ##############")

    producer.close()
  }

  def createConsumerConfig(brokers: String): Properties = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "1")
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "500")
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props
  }

}
