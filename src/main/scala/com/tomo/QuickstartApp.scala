package com.tomo

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import com.tomo.actors.EventManagerRegistryActor
import com.tomo.config.Config
import com.tomo.routes.EventManagerRoutes

import scala.concurrent.Await
import scala.concurrent.duration.Duration

//#main-class
object QuickstartApp extends App with Config with EventManagerRoutes {

  implicit val system: ActorSystem = ActorSystem("PhotoboxHttpServer")

  val host = serverConfig.getString("host")
  val port = serverConfig.getInt("port")

  val eventManagerRegistryActor: ActorRef = system.actorOf(EventManagerRegistryActor.props(), "eventManagerRegistry")

  Http()
    .newServerAt(host, port)
    .bindFlow(eventManagerRoutes)

  println(s"Server online at http://$host:$port")

  Await.result(system.whenTerminated, Duration.Inf)
}
//#main-class
