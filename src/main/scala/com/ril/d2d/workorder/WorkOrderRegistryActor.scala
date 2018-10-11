package com.ril.d2d.workorder

import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.util.Timeout
import com.ril.d2d.kafka.Event
import com.ril.d2d.kafka.KafkaConsumerActor.Consume
import com.ril.d2d.kafka.KafkaProducerActor.Produce

import scala.concurrent.duration._
import akka.pattern.ask

import scala.concurrent.Future

final case class WorkOrder(workOrderId: Long, workType: String, scheduledDateTime: String, address: String, status: String)
final case class WorkOrders(workOrders: Seq[WorkOrder])

object WorkOrderRegistryActor {
  final case class GetWorkOrders(correlationId: String)

  def props(kafkaProducerActor: ActorRef, kafkaConsumerActor: ActorRef): Props =
    Props(new WorkOrderRegistryActor(kafkaProducerActor, kafkaConsumerActor))

  implicit lazy val timeout = Timeout(5.seconds)
}

class WorkOrderRegistryActor(kafkaProducerActor: ActorRef, kafkaConsumerActor: ActorRef) extends Actor with ActorLogging {
  import WorkOrderRegistryActor._

  //  var workOrders = Set(WorkOrder(12345, "Fiber Installation Only", LocalDateTime.now().toString(), "Navi Mumbai", "Assigned"))

  def receive: Receive = {
    case GetWorkOrders(correlationId) =>
      println(" $$$$$ WorkOrderRegistryActor handling GetWorkOrders")
      kafkaProducerActor ! Produce(Event("GetWorkOrders", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), correlationId, None))
      kafkaConsumerActor ! Consume("GetWorkOrders", correlationId, sender())
  }
}