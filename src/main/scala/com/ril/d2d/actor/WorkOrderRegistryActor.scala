package com.ril.d2d.actor

import java.time.LocalDateTime

import akka.actor.{ Actor, ActorLogging, Props }

final case class WorkOrder(workOrderId: Long, workType: String, scheduledDateTime: String, address: String, status: String)
final case class WorkOrders(workOrders: Seq[WorkOrder])

object WorkOrderRegistryActor {
  final case object GetWorkOrders

  def props: Props = Props[WorkOrderRegistryActor]
}

class WorkOrderRegistryActor extends Actor with ActorLogging {
  import WorkOrderRegistryActor._

  var workOrders = Set(WorkOrder(12345, "Fiber Installation Only", LocalDateTime.now().toString(), "Navi Mumbai", "Assigned"))

  def receive: Receive = {
    case GetWorkOrders =>
      sender() ! WorkOrders(workOrders.toSeq)
  }
}