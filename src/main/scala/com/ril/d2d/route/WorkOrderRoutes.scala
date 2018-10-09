package com.ril.d2d.route

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.server.Directives.{ pathEnd, pathPrefix }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.ril.d2d.actor.WorkOrderRegistryActor.GetWorkOrders
import com.ril.d2d.actor.WorkOrders
import com.ril.d2d.json.JsonSupport

import scala.concurrent.Future
import scala.concurrent.duration._

trait WorkOrderRoutes extends JsonSupport {

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
