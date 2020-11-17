package com.tomo.model

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}

import scala.util.Try

object DateProtocol extends DefaultJsonProtocol {
  implicit object DateFormat extends JsonFormat[Date] {
    override def write(date: Date): JsValue = JsString(sdf.format(date))

    override def read(json: JsValue): Date = json match {
      case JsString(rawDate) =>
        Try(sdf.parse(rawDate))
          .getOrElse(throw DeserializationException("Cannot parse date value"))
      case error =>
        throw DeserializationException(s"Expected date as JsString but got ${error.getClass}")
    }
  }

  val sdf = {
    val TIME_ZONE = TimeZone.getTimeZone("UTC")
    val TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    val sdf = new SimpleDateFormat(TIMESTAMP_FORMAT)
    sdf.setTimeZone(TIME_ZONE)
    sdf
  }
}
