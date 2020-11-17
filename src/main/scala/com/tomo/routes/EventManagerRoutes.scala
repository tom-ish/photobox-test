package com.tomo.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.tomo.actors.EventManagerRegistryActor
import com.tomo.actors.EventManagerRegistryActor.{ActionPerformed, CreateOrUpdateEvent, DeleteEvent, GetPhotosUsage, PhotosUsage}
import com.tomo.model.Entities.Event
import com.tomo.model.{Entities, JsonSupport}
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait EventManagerRoutes extends SprayJsonSupport
  with DefaultJsonProtocol
  with JsonSupport {
  import spray.json._
  import akka.http.scaladsl.server.Directives._
  lazy val log = Logging(system, classOf[EventManagerRoutes])

  implicit def system: ActorSystem
  implicit lazy val timeout = Timeout(5 seconds)

  def eventManagerRegistryActor: ActorRef

  lazy val eventManagerRoutes: Route =
    concat(
      pathPrefix("notifUsage") {
        pathEndOrSingleSlash {
          post {
            entity(as[Event]) { event =>
              event.eventType match {
                case operation if operation == Entities.CREATE || operation == Entities.UPDATE =>
                  val eventCreatedFuture: Future[ActionPerformed] =
                    (eventManagerRegistryActor ? CreateOrUpdateEvent(event)).mapTo[ActionPerformed]
                  onSuccess(eventCreatedFuture) {
                    case EventManagerRegistryActor.Failure(description) =>
                      log.warning("{} {}", description, event.eventId)
                      complete(StatusCodes.NotFound)
                    case EventManagerRegistryActor.Successful(description) =>
                      log.info("{}", description)
                      complete(StatusCodes.NoContent)
                  }
                case error =>
                  log.warning("Tried to call notifUsage with invalid operation: {}", error)
                  complete(StatusCodes.BadRequest)
              }
            }
          } ~
          delete {
            entity(as[Event]) { event =>
              event.eventType match {
                case Entities.DELETE =>
                  val eventDeletedFuture: Future[ActionPerformed] =
                    (eventManagerRegistryActor ? DeleteEvent(event)).mapTo[ActionPerformed]
                  onSuccess(eventDeletedFuture) {
                    case EventManagerRegistryActor.Failure(description) =>
                    log.warning("{} {}", description, event.eventId)
                    complete(StatusCodes.NotFound)
                    case EventManagerRegistryActor.Successful(description) =>
                    log.info("{}", description)
                    complete(StatusCodes.NoContent)
                  }
                case error =>
                  log.warning("Tried to call notifUsage with invalid operation: {}", error)
                  complete(StatusCodes.BadRequest)
              }
            }
          }
        }
      },
      pathPrefix("getPhotoUsage") {
        parameter("photoIds") { rawPhotoIds =>
          if(rawPhotoIds.isEmpty)
            complete(StatusCodes.NoContent)
          else {
            val photoIds: Set[Int] = rawPhotoIds
              .split(",").toList
              .map(Integer.valueOf(_).intValue).toSet

            val getSources: Future[PhotosUsage] =
              (eventManagerRegistryActor ? GetPhotosUsage(photoIds)).mapTo[PhotosUsage]
            onSuccess(getSources) { results =>
              if(results.content.isEmpty)
                complete(StatusCodes.NoContent)
              else
                complete(StatusCodes.OK, results.content.toJson)
            }
          }
        }
      }
    )
}
