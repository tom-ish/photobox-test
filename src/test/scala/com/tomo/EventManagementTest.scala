package com.tomo

import java.util.{Date, UUID}

import com.tomo.model.Entities.Event
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable

class EventManagementTest extends WordSpec with Matchers {

  "Data retrieval" should {
    "insert event correctly" in {
      // given
      val events = mutable.Map.empty[UUID, Event]
      val eventId = UUID.randomUUID()
      val source = "studio"
      val sourceId = UUID.randomUUID()
      val eventType = "CREATE"
      val date = new Date()
      val photoIds = Set(1, 2, 3, 4)
      val e = Event(eventId, source, sourceId, eventType, date, photoIds)

      // when
      events.put(eventId, e)

      // then
      events.size should be(1)
    }
    "update event correctly" in {
      // given
      val events = mutable.Map.empty[UUID, Event]

      val eventId1 = UUID.randomUUID()
      val source1 = "studio"
      val sourceId1 = UUID.randomUUID()
      val eventType1 = "CREATE"
      val date1 = new Date()
      val photoIds1 = Set(1, 2, 3, 4)
      val e1 = Event(eventId1, source1, sourceId1, eventType1, date1, photoIds1)

      val eventId2 = UUID.randomUUID()
      val source2 = "studio"
      val sourceId2 = UUID.randomUUID()
      val eventType2 = "CREATE"
      val date2 = new Date()
      val photoIds2 = Set(5, 6)
      val e2 = Event(eventId2, source2, sourceId2, eventType2, date2, photoIds2)

      val eventId3 = eventId1
      val source3 = "studio"
      val sourceId3 = UUID.randomUUID()
      val eventType3 = "UPDATE"
      val date3 = new Date()
      val photoIds3 = Set(7, 8, 9)
      val e3 = Event(eventId3, source3, sourceId3, eventType3, date3, photoIds3)

      // when
      events.put(eventId1, e1)
      events.put(eventId2, e2)
      events.put(eventId3, e3)

      // then
      events.size should be(2)
      events(eventId1).photoIds.size should be(3)
      events(eventId1).photoIds should be(Set(7, 8, 9))
    }
    "delete event correctly" in {
      // given
      val events = mutable.Map.empty[UUID, Event]

      val studioSource = "studio"
      val studioSourceId = UUID.randomUUID()
      val studioSourcePath = s"$studioSource:$studioSourceId"
      println(s"studio path: $studioSourcePath")

      val editorSource = "editor"
      val editorSourceId = UUID.randomUUID()
      val editorSourcePath = s"$editorSource:$editorSourceId"
      println(s"editor path: $editorSourcePath")

      val eventId1 = UUID.randomUUID()
      val source1 = studioSource
      val sourceId1 = studioSourceId
      val eventType1 = "CREATE"
      val date1 = new Date()
      val photoIds1 = Set(1, 2, 3, 4)
      val e1 = Event(eventId1, source1, sourceId1, eventType1, date1, photoIds1)

      val eventId2 = UUID.randomUUID()
      val source2 = studioSource
      val sourceId2 = studioSourceId
      val eventType2 = "CREATE"
      val date2 = new Date()
      val photoIds2 = Set(5, 6)
      val e2 = Event(eventId2, source2, sourceId2, eventType2, date2, photoIds2)

      val source3 = editorSource
      val sourceId3 = editorSourceId
      val eventType3 = "UPDATE"
      val date3 = new Date()
      val e3 = Event(eventId1, source3, sourceId3, eventType3, date3, Set.empty)

      events.put(eventId1, e1)
      events.put(eventId2, e2)

      // when
      events.put(eventId1, e3)

      // then
      events.size should be(2)
      events(eventId1).photoIds.size should be(0)
      events(eventId1).photoIds should be(Set.empty)
    }
    "retrieve source and sourceId correctly depending on photoId" in {
      // given
      val events = mutable.Map.empty[UUID, Event]

      val studioSource = "studio"
      val studioSourceId = UUID.randomUUID()
      val studioSourcePath = s"$studioSource:$studioSourceId"
      println(s"studio path: $studioSourcePath")

      val editorSource = "editor"
      val editorSourceId = UUID.randomUUID()
      val editorSourcePath = s"$editorSource:$editorSourceId"
      println(s"editor path: $editorSourcePath\n")

      val eventId1 = UUID.randomUUID()
      val source1 = studioSource
      val sourceId1 = studioSourceId
      val eventType1 = "CREATE"
      val date1 = new Date()
      val photoIds1 = Set(1, 2, 3, 4)
      val e1 = Event(eventId1, source1, sourceId1, eventType1, date1, photoIds1)

      val eventId2 = UUID.randomUUID()
      val source2 = editorSource
      val sourceId2 = editorSourceId
      val eventType2 = "CREATE"
      val date2 = new Date()
      val photoIds2 = Set(1, 6)
      val e2 = Event(eventId2, source2, sourceId2, eventType2, date2, photoIds2)

      val eventId3 = UUID.randomUUID()
      val source3 = studioSource
      val sourceId3 = studioSourceId
      val eventType3 = "UPDATE"
      val date3 = new Date()
      val photoIds3 = Set(2, 8, 9)
      val e3 = Event(eventId3, source3, sourceId3, eventType3, date3, photoIds3)

      events.put(eventId1, e1)
      events.put(eventId2, e2)
      events.put(eventId3, e3)

      // we are looking for photoIds = {1, 2}
      // photoId = 1 should contains Set("studio:studioSourceId", "editor:editorSourceId")
      // photoId = 2 should contains Set("studio:studioSourceId")
      // photoId = 6 should contains Set("editor:editorSourceId")
      val photoIds = Set(1, 2, 6)

      // when
      val results: Set[Map[String, Set[String]]] = photoIds.map { photoId =>
        events.values
          .filter(event => event.photoIds.contains(photoId)) // filter events containing photoId
          .map(event => photoId -> s"${event.source}:${event.sourceId}") // create a mapping between photoId and event
          .groupBy(_._1) // group by photoId
          .map(item => String.valueOf(item._1) -> item._2) // convert to Map[String, String] because Spray does not handle Map which key type is not String
          .view.mapValues(_.map(_._2).toSet)
          .toMap
      }

      // then
      results.foreach(_.foreach(println))
      results.size should be(3)
      results should be(
        Set(
          Map(("1", Set(editorSourcePath, studioSourcePath))),
          Map(("2", Set(studioSourcePath))),
          Map(("6", Set(editorSourcePath)))
        )
      )
    }
  }
}
