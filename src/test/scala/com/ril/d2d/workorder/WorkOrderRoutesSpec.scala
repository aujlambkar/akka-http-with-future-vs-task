package com.ril.d2d.workorder

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.ril.d2d.kafka.KafkaConsumerActor.StartPolling
import com.ril.d2d.kafka.{KafkaConsumerActor, KafkaProducerActor, ResponseHandlerActor}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class WorkOrderRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with WorkOrderRoutes {

  private val broker = "100.96.8.53:9092"

  val responseHandleActor: ActorRef = system.actorOf(ResponseHandlerActor.props, "responseHandlerActor")
  val kafkaConsumerActor: ActorRef = system.actorOf(KafkaConsumerActor.props(broker, "response", "1", responseHandleActor))
  val kafkaProducerActor: ActorRef = system.actorOf(KafkaProducerActor.props(broker, "request"))

  kafkaConsumerActor ! StartPolling

  override val workOrderRegistryActor: ActorRef =
    system.actorOf(WorkOrderRegistryActor.props(kafkaProducerActor, responseHandleActor), "workOrderRegistry")

  lazy val routes = workOrderRoutes

  "WorkOrder API" should {
    "give all Work Orders (GET /workorders)" in {
      val request = HttpRequest(uri = "/workorders")

      request ~> routes ~> check {
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
