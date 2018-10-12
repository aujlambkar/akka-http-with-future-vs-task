package com.ril.d2d.workorder

import java.time.{LocalDateTime, ZoneOffset}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import com.ril.d2d.kafka.Event
import com.ril.d2d.kafka.KafkaProducerActor.Produce
import com.ril.d2d.kafka.ResponseHandlerActor.RegisterResponseHandler

import scala.concurrent.duration._

final case class WorkOrder(workOrderId: Long, workType: String, scheduledDateTime: String, address: String, status: String)
final case class WorkOrders(workOrders: Seq[WorkOrder])

object WorkOrderRegistryActor {
  final case class GetWorkOrders(correlationId: String)

  def props(kafkaProducerActor: ActorRef, responseHandlerActor: ActorRef): Props =
    Props(new WorkOrderRegistryActor(kafkaProducerActor, responseHandlerActor))

  implicit lazy val timeout = Timeout(5.seconds)
}

class WorkOrderRegistryActor(kafkaProducerActor: ActorRef, responseHandlerActor: ActorRef) extends Actor with ActorLogging {
  import WorkOrderRegistryActor._

  def receive: Receive = {
    case GetWorkOrders(correlationId) =>
      responseHandlerActor ! RegisterResponseHandler("GetWorkOrders", correlationId, sender())
      kafkaProducerActor ! Produce(Event("GetWorkOrders", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), correlationId, None))
  }
}