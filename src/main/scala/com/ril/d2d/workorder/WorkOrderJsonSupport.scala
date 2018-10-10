package com.ril.d2d.workorder

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait WorkOrderJsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val workOrderJsonFormat = jsonFormat5(WorkOrder)
  implicit val workOrdersJsonFormat = jsonFormat1(WorkOrders)

}
