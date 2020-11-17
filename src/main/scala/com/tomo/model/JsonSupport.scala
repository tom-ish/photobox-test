package com.tomo.model

import com.tomo.model.Entities.Event
import spray.json._

trait JsonSupport extends DefaultJsonProtocol {
  import DateProtocol._
  import UUIDProtocol._
  implicit val eventJsonFormat = jsonFormat6(Event)
}