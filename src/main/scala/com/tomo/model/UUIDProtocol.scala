package com.tomo.model

import java.util.UUID

import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}

import scala.util.Try

object UUIDProtocol extends DefaultJsonProtocol {
  implicit object UUIDFormat extends JsonFormat[UUID] {
    override def write(uuid: UUID): JsValue = JsString(uuid.toString)

    override def read(json: JsValue): UUID = json match {
      case JsString(value) =>
        Try(UUID.fromString(value))
          .getOrElse(throw DeserializationException("Cannot retrieve uuid value"))
      case error =>
        throw DeserializationException(s"Expected UUID as JsString but got ${error.getClass}")
    }
  }
}
