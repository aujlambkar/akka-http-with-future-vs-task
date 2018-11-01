package com.ril.d2d

import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Route}
import akka.util.Timeout
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import scala.collection.mutable.HashMap
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.http.scaladsl.server.Directives._
import scala.concurrent.duration._

trait AppRoutes {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[AppRoutes])

  var correlationMap: HashMap[String, String] = HashMap.empty[String, String]

  implicit lazy val timeout = Timeout(20.seconds)

  lazy val workOrderRoutes: Route = {
    get {
//        path("wo") {
//        val correlationId: String = LocalDateTime.now().toEpochSecond(UTC).toString
//        val producerFuture = Future { produce(correlationId) }
//
//          println("Main: " + Thread.currentThread().getName)
//          producerFuture.onComplete {
//            case scala.util.Success(value) => {
//              Future { consume(correlationId) }
//            }
//            case scala.util.Failure(exception) => {
//              println("Failure on producer")
//            }
//          }
//          println("Before request completeion")
//        complete(StatusCodes.Created -> producerFuture)
//      } ~
//      path("wo" / Segment) { correlationId: String => {
//          val resultsFuture = Future { getResult(correlationId) }
//          complete(resultsFuture)
//        }
//      } ~
      path("wo") {
        val correlationId: String = LocalDateTime.now().toEpochSecond(UTC).toString
        val producerTask = Task { produce(correlationId) }
        val consumerTask = Task { consume(correlationId) }

        println("Main: " + Thread.currentThread().getName)

        println("Before request completion")

        val producerFuture = producerTask.runAsync

        producerFuture.onComplete {
          case Success(value) =>
            println("Producer Task Success " + value)
          case Failure(exception) =>
            println(s"Producer Task Failed ${exception.getMessage}")
        }

        consumerTask.runAsync

        complete(StatusCodes.Created -> producerFuture)
      } ~
      path("wo" / Segment) { correlationId: String => {
        val resultsFuture = Future { getResult(correlationId) }
        complete(resultsFuture)
      }
      }
    }
  }

  private def produce(correlationId: String) = {
    println("Producer Start Thread: " + Thread.currentThread().getName)
    correlationMap.put(correlationId, "looking for workorders for " + correlationId)
    println("Producer Finish Thread: " + Thread.currentThread().getName)
    correlationId
  }

  private def consume(correlationId: String) = {
    println("Consumer Start Thread: " + Thread.currentThread().getName)
    Thread.sleep(10000)
    correlationMap.put(correlationId, "workorders found for " + correlationId)
    println("Consumer Finish Thread: " + Thread.currentThread().getName)
    correlationId
  }

  private def getResult(correlationId: String) = {
    println("Get Results Thread: " + Thread.currentThread().getName)
    correlationMap.get(correlationId)
  }
}
