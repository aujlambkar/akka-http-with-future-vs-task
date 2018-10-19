package com.ril.d2d.workorder

import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import com.ril.d2d.kafka.KafkaConsumerActor.StartPolling
import com.ril.d2d.kafka._
import net.manub.embeddedkafka.{ EmbeddedKafka, EmbeddedKafkaConfig }
import org.apache.kafka.clients.producer.KafkaProducer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._

class WorkOrderRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with WorkOrderRoutes with EmbeddedKafka with KafkaJsonSupport with WorkOrderJsonSupport {

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(20.second)

  val broker = "127.0.0.1:6001"
  val requestTopic = "request"
  val responseTopic = "response"

  val producer = new KafkaProducer[String, String](KafkaProducerActor.producerConf(broker))
  val responseHandleActor: ActorRef = system.actorOf(ResponseHandlerActor.props, "responseHandlerActor")
  val kafkaConsumerActor: ActorRef = system.actorOf(KafkaConsumerActor.props(broker, responseTopic, "1", responseHandleActor))
  val kafkaProducerActor: ActorRef = system.actorOf(KafkaProducerActor.props(producer, requestTopic))
  kafkaConsumerActor ! StartPolling

  override val workOrderRegistryActor: ActorRef =
    system.actorOf(WorkOrderRegistryActor.props(kafkaProducerActor, responseHandleActor), "workOrderRegistry")

  lazy val routes = workOrderRoutes
  "Walking skeleton" should {
    "give stubbed work orders through kafka topics" in {
      withRunningKafka {
        Future {
          val publishedMessage = consumeFirstStringMessageFrom(requestTopic)
          val corelationId = getCorelationIdFromPublishedMessage(publishedMessage)
          publishStringMessageToKafka(responseTopic, getStubResponse(corelationId))
        }

        HttpRequest(uri = "/workorders") ~> routes ~> check {
          status should ===(StatusCodes.OK)
          contentType should ===(ContentTypes.`application/json`)

          val actualWorkOrder = entityAs[WorkOrders].workOrders.head
          actualWorkOrder.workOrderId should ===(12345.toLong)
          actualWorkOrder.workType should ===("Fiber Installation Only")
          actualWorkOrder.address should ===("Navi Mumbai")
          actualWorkOrder.status should ===("Assigned")
        }
      }
    }
  }

  def getCorelationIdFromPublishedMessage(message: String): String = {
    val event = message.parseJson.convertTo[Event]
    event.name should ===("GetWorkOrders")
    event.correlationId
  }

  def getStubResponse(corelationId: String): String = {
    var workOrders = WorkOrders(Seq(WorkOrder(12345, "Fiber Installation Only", LocalDateTime.now().toString(), "Navi Mumbai", "Assigned")))
    Event("GetWorkOrders", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), corelationId, Some(workOrders.toJson.compactPrint)).toJson.compactPrint
  }

}
