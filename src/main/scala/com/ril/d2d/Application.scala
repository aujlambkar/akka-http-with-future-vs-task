package com.ril.d2d

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object Application extends App with AppRoutes {

  implicit val system: ActorSystem = ActorSystem("activityDiscovery")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val config = ConfigFactory.load
  private val hostIP = config.getString("host.ip")
  private val hostPort = config.getInt("host.port")

  lazy val routes: Route = workOrderRoutes

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, hostIP, hostPort)

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
