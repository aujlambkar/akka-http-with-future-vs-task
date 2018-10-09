package com.ril.d2d

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.ril.d2d.actor.WorkOrderRegistryActor
import com.ril.d2d.route.WorkOrderRoutes

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success }

object Application extends App with WorkOrderRoutes {

  implicit val system: ActorSystem = ActorSystem("activityDiscovery")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val workOrderRegistryActor: ActorRef = system.actorOf(WorkOrderRegistryActor.props, "workOrderRegistryActor")

  lazy val routes: Route = workOrderRoutes

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "0.0.0.0", 7001)

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
