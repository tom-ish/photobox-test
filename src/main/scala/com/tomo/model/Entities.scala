package com.tomo.model

import java.util.{Date, UUID}

object Entities {
  final val CREATE = "CREATE"
  final val UPDATE = "UPDATE"
  final val DELETE = "DELETE"
  final case class Event(eventId: UUID, source: String, sourceId: UUID, eventType: String, date: Date, photoIds: Set[Int])
  final case class Events(events: Set[Event])
}