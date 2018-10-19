package com.ril.d2d.kafka

import akka.actor.{ Actor, ActorRef, Props }
import com.ril.d2d.kafka.ResponseHandlerActor.{ HandleResponse, RegisterResponseHandler, ResponseCorrelation }
import com.ril.d2d.workorder.{ WorkOrderJsonSupport, WorkOrders }
import spray.json._

object ResponseHandlerActor {

  def props = Props[ResponseHandlerActor]

  final case class RegisterResponseHandler(eventName: String, correlationId: String, actorRef: ActorRef)

  final case class ResponseCorrelation(eventName: String, correlationId: String, actorRef: ActorRef)

  final case class HandleResponse(event: Event)

}

class ResponseHandlerActor extends Actor with WorkOrderJsonSupport {
  var responseHandlerRegistry = Set.empty[ResponseCorrelation]

  override def receive: Receive = {
    case RegisterResponseHandler(eventName, correlationId, actorRef) => {
      responseHandlerRegistry += ResponseCorrelation(eventName, correlationId, actorRef)
    }
    case HandleResponse(event) => handleRecord(event)
  }

  def handleRecord(event: Event) = {
    responseHandlerRegistry
      .find(handler => handler.correlationId.equals(event.correlationId))
      .foreach(handler => {
        responseHandlerRegistry -= handler
        handler.actorRef ! event.payload.map(_.parseJson.convertTo[WorkOrders]).getOrElse(WorkOrders(Seq()))
      })
  }
}
