package com.tomo.routes

import java.text.SimpleDateFormat
import java.util.UUID

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.tomo.actors.EventManagerRegistryActor
import com.tomo.model.Entities.Event
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class EventManagerRoutesSpec
  extends WordSpec
  with Matchers
  with ScalaFutures
  with ScalatestRouteTest
  with EventManagerRoutes {

  import spray.json._

  override val eventManagerRegistryActor: ActorRef = system.actorOf(EventManagerRegistryActor.props(), "eventManagerRegistry")

  lazy val routes = eventManagerRoutes

  "EventManagerRoutes" should {
    "[POST /notifUsage] return 204 NO_CONTENT on event creation" in {
      //given
      val eventId = UUID.fromString("f7c80f1e-1d08-11eb-9e2e-f74124011ce0")
      val sourceId = UUID.fromString("5b59848c-1d12-11eb-9951-6fcd478d7428")
      val dateStr = "2020-11-02T12:44:36.685Z"
      val date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(dateStr)
      val photoIds = Set(2136515454, 2136515455, 2136515456, 2136515457)

      val event = Event(
        eventId = eventId,
        source = "studio",
        sourceId = sourceId,
        eventType = "CREATE",
        date = date,
        photoIds = photoIds
      )

      val requestEntity = HttpEntity(
        contentType = MediaTypes.`application/json`,
        event.toJson.toString()
      )

      // when
      Post("/notifUsage").withEntity(requestEntity) ~> routes ~> check {
        // then
        status should be(StatusCodes.NoContent)
      }
    }

    "[POST /notifUsage] return BAD_REQUEST on event creation" in {
      //given
      val eventId = UUID.fromString("f7c80f1e-1d08-11eb-9e2e-f74124011ce0")
      val sourceId = UUID.fromString("5b59848c-1d12-11eb-9951-6fcd478d7428")
      val dateStr = "2020-11-02T12:44:36.685Z"
      val date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(dateStr)
      val photoIds = Set(2136515454, 2136515455, 2136515456, 2136515457)

      val event = Event(
        eventId = eventId,
        source = "studio",
        sourceId = sourceId,
        eventType = "TOTO",
        date = date,
        photoIds = photoIds
      )

      val requestEntity = HttpEntity(
        contentType = MediaTypes.`application/json`,
        event.toJson.toString()
      )

      // when
      Post("/notifUsage").withEntity(requestEntity) ~> routes ~> check {
        // then
        status should be(StatusCodes.BadRequest)
      }
    }

    "[DELETE /notifUsage] be able to remove photos in event" in {
      //given
      val eventId = UUID.fromString("f7c80f1e-1d08-11eb-9e2e-f74124011ce0")
      val sourceId = UUID.fromString("5b59848c-1d12-11eb-9951-6fcd478d7428")
      val dateStr = "2020-11-02T12:44:36.685Z"
      val date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(dateStr)

      val event = Event(
        eventId = eventId,
        source = "studio",
        sourceId = sourceId,
        eventType = "DELETE",
        date = date,
        photoIds = Set.empty
      )

      val requestEntity = HttpEntity(
        contentType = MediaTypes.`application/json`,
        event.toJson.toString()
      )

      // when
      Delete("/notifUsage").withEntity(requestEntity) ~> routes ~> check {
        // then
        status should be(StatusCodes.NoContent)
      }
    }

    "[GET /getPhotoUsage] retrieve events" in {
      // given/when
      Get("/getPhotoUsage?photoIds=1") ~> routes ~> check {
        // then
        status should be(StatusCodes.OK)
      }
    }
  }
}