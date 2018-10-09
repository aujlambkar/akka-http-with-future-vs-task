package com.ril.d2d.route

import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest._


class LearnKafkaSpec extends WordSpec with Matchers {

  "Kafka" should {
    val brokers = "100.96.8.107:9092"

    "produce message" in {

      produceMessage(brokers, "test")

      1 should be (1)
    }

    "consume messages" in {
      val props = new Properties()
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "1")
      props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
      props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    }
  }



  def produceMessage(brokers: String, topic: String) {

    val props = new Properties()
    props.put("bootstrap.servers", brokers)
    props.put("client.id", "ScalaProducerExample")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    val data: ProducerRecord[String, String] = new ProducerRecord[String, String](topic, "any key", "Test message 123")

    println("################### Started Message sent ##############")
    producer.send(data)
    println("################### Competed Message sent ##############")

    producer.close()
  }

  def startConsumer(): Unit = {

  }

}
