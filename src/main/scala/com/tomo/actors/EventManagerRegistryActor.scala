package com.tomo.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.tomo.model.Entities
import com.tomo.model.Entities.Event

import scala.collection.mutable
import scala.language.postfixOps

object EventManagerRegistryActor {
  sealed trait Response
  sealed trait ActionPerformed extends Response
  final case class Successful(description: String) extends ActionPerformed
  final case class Failure(description: String) extends ActionPerformed
  final case class PhotosUsage(content: Set[Map[String, Set[String]]]) extends Response

  sealed trait Request
  final case object GetEvents extends Request
  final case class GetPhotosUsage(photoIds: Set[Int]) extends Request
  final case class GetPhotoUsage(photoId: Int) extends Request
  final case class CreateOrUpdateEvent(event: Event) extends Request
  final case class DeleteEvent(event: Event) extends Request

  def props() = Props(new EventManagerRegistryActor)
}

class EventManagerRegistryActor extends Actor with ActorLogging {
  import EventManagerRegistryActor._

  val events = mutable.Map.empty[UUID, Event]

  override def receive: Receive = {
    case CreateOrUpdateEvent(e) =>
      e.eventType match {
        case Entities.CREATE =>
          if(!events.contains(e.eventId)) {
            events.put(e.eventId, e)
            sender() ! Successful(s"Event ${e.eventId} created.")
          }
          else
            sender() ! Failure(s"${e.eventId} already exists.")

        case Entities.UPDATE =>
          if(events.contains(e.eventId)) {
            events.put(e.eventId, e)
            sender() ! Successful(s"Event ${e.eventId} updated.")
          }
          else
            sender() ! Failure(s"${e.eventId} not found.")

        case operation =>
          sender() ! Failure(s"Illegal operation on CreateOrUpdateEvent: $operation")
      }

    case DeleteEvent(e) =>
      if(events.contains(e.eventId)) {
        val deletedPhotoIds = e.photoIds
        val updatedEvent = Event(e.eventId, e.source, e.sourceId, e.eventType, e.date, Set.empty)
        events.put(e.eventId, updatedEvent)
        sender() ! Successful(s"Deleted $deletedPhotoIds from Event ${e.eventId}.")
      }
      else
        sender() ! Failure(s"${e.eventId} not found.")

    case GetPhotosUsage(photoIds) =>
      val results: Set[Map[String, Set[String]]] = photoIds.map { photoId =>
        events.values
          .filter(event => event.photoIds.contains(photoId)) // filter events containing photoId
          .map(event => photoId -> s"${event.source}:${event.sourceId}") // create a mapping between photoId and event
          .groupBy(_._1) // group by photoId
          .map(item => String.valueOf(item._1) -> item._2) // convert to Map[String, String] because Spray does not handle Map which key type is not String
          .view.mapValues(_.map(_._2).toSet)
          .toMap
      }

      log.info(s"$results")
      sender() ! PhotosUsage(results)

    case GetPhotoUsage(photoId) =>
      val result: Map[String, Set[String]] = events.values
        .filter(event => event.photoIds.contains(photoId)) // filter events containing photoId
        .map(event => photoId -> s"${event.source}:${event.sourceId}") // create a mapping between photoId and event
        .groupBy(_._1) // group by photoId
        .map(item => String.valueOf(item._1) -> item._2) // convert to Map[String, String]
        .view.mapValues(_.map(_._2).toSet).toMap
      sender() ! result
  }
}
