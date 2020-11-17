package com.tomo.model

import java.util.UUID

import com.tomo.model.Entities.Event
import org.scalatest.{Matchers, WordSpec}

import scala.language.postfixOps

class ConversionTest extends WordSpec with Matchers with JsonSupport {
  import spray.json._

  "Conversion" should {
    "convert correctly" in {
      // given
      val uuidStr = "f7c80f1e-1d08-11eb-9e2e-f74124011ce0"

      // when
      val uuid = UUIDProtocol.UUIDFormat.write(UUID.fromString(uuidStr))

      // then
      assert(uuid.toJson == JsString("f7c80f1e-1d08-11eb-9e2e-f74124011ce0"))
    }

    "convert correctly date" in {
      // given
      val dateStr = "2020-11-02T12:44:36.685Z"
      val date = DateProtocol.DateFormat.write(DateProtocol.sdf.parse(dateStr))

      assert(date.toJson == JsString("2020-11-02T12:44:36.685Z"))
    }

    "convert correctly event" in {
      // given
      val eventId = UUID.fromString("f7c80f1e-1d08-11eb-9e2e-f74124011ce0")
      val sourceId = UUID.randomUUID()
      val dateStr = "2020-11-02T12:44:36.685Z"
      val date = DateProtocol.sdf.parse(dateStr)
      val photoIds = Set(123, 234, 345, 456)

      val event = Event(
        eventId = eventId,
        source = "studio",
        sourceId = sourceId,
        eventType = "CREATE",
        date = date,
        photoIds = photoIds
      )

      println(event.toJson.toString)
    }

    "parse correctly event" in {
      // given
      val eventId = UUID.fromString("f7c80f1e-1d08-11eb-9e2e-f74124011ce0")
      val sourceId = UUID.fromString("5b59848c-1d12-11eb-9951-6fcd478d7428")
      val dateStr = "2020-11-02T12:44:36.685Z"
      val date = DateProtocol.sdf.parse(dateStr)
      val photoIds = Set(2136515454, 2136515455, 2136515456, 2136515457)

      val eventJson =
        """
          |{
          |    "eventId" : "f7c80f1e-1d08-11eb-9e2e-f74124011ce0",
          |    "source" : "studio",
          |    "sourceId" : "5b59848c-1d12-11eb-9951-6fcd478d7428",
          |    "eventType": "CREATE",
          |    "date" : "2020-11-02T12:44:36.685Z",
          |    "photoIds" : [ 2136515454, 2136515455, 2136515456, 2136515457]
          |}
          |""".stripMargin

      val event = Event(
        eventId = eventId,
        source = "studio",
        sourceId = sourceId,
        eventType = "CREATE",
        date = date,
        photoIds = photoIds
      )

      assert(event.toJson == eventJson.parseJson)
    }
  }
}
