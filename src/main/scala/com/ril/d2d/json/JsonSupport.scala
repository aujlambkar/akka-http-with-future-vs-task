package com.ril.d2d.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.ril.d2d.actor.{ WorkOrder, WorkOrders }
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val workOrderJsonFormat = jsonFormat5(WorkOrder)
  implicit val workOrdersJsonFormat = jsonFormat1(WorkOrders)

}
