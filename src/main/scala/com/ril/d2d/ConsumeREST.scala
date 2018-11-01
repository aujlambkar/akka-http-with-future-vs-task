package com.ril.d2d

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import scala.util.{Failure, Success}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode}
import akka.stream.ActorMaterializer

import scala.concurrent.Future

object ConsumeREST extends App {
  val url = "http://localhost:7001/wo"
  val result = scala.io.Source.fromURL(url).mkString
  println(result)
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  httpGet(url)

  private def httpGet(url: String) = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url))

    responseFuture
      .onComplete {
        case Success(res) => {
//          if (res.status.intValue() == 201) {
//            httpGet("http://localhost:7001/wo")
//
//          }
            println(res)
        }
        case Failure(_) => sys.error("something wrong")
      }
  }
}