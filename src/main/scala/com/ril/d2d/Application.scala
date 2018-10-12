package com.ril.d2d

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.ril.d2d.kafka.KafkaConsumerActor.StartPolling
import com.ril.d2d.kafka.{KafkaConsumerActor, KafkaProducerActor, ResponseHandlerActor}
import com.ril.d2d.workorder.{WorkOrderRegistryActor, WorkOrderRoutes}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object Application extends App with WorkOrderRoutes {

  implicit val system: ActorSystem = ActorSystem("activityDiscovery")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val broker = "100.96.8.53:9092"


  val responseHandleActor: ActorRef = system.actorOf(ResponseHandlerActor.props, "responseHandlerActor")
  val kafkaConsumerActor: ActorRef = system.actorOf(KafkaConsumerActor.props(broker, "response", "1", responseHandleActor), "kafkaConsumerActor")
  val kafkaProducerActor: ActorRef = system.actorOf(KafkaProducerActor.props(broker, "request"), "kafkaProducerActor")
  val workOrderRegistryActor: ActorRef = system.actorOf(WorkOrderRegistryActor.props(kafkaProducerActor, responseHandleActor), "workOrderRegistryActor")

  kafkaConsumerActor ! StartPolling

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
