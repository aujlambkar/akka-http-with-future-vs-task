package com.ril.d2d.workorder

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.server.Directives.{pathEnd, pathPrefix}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import com.ril.d2d.workorder.WorkOrderRegistryActor.GetWorkOrders

import scala.concurrent.Future

trait WorkOrderRoutes extends WorkOrderJsonSupport {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[WorkOrderRoutes])

  def workOrderRegistryActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds)

  lazy val workOrderRoutes: Route =
    pathPrefix("workorders") {
      pathEnd {
        get {
          val workorders: Future[WorkOrders] = (workOrderRegistryActor ? GetWorkOrders).mapTo[WorkOrders]
          complete(workorders)
        }
      }
    }
}
