package com.zhangmin.meetup

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{BroadcastHub, Flow, Source}
import com.zhangmin.meetup.config.{LoadProducerSettings, SettingsLoader}
import com.zhangmin.meetup.utils.Resources._
import com.zhangmin.meetup.utils.EventGenerator
import pureconfig.generic.auto._

import scala.concurrent.duration._

/**
  * App to infinitely produce RSVP events from the event template.
  * The event will have 1 to N random topics, a random city and country,
  * and uses `System.currentTimeMillis` for the event time.
  *
  * The default websocket url is `ws://localhost:8080`.
  *
  * This is needed because the Meetup websocket stream API (ws://stream.meetup.com/2/rsvps)
  * is deprecated and the new API requires credentials.
  */
object LoadProducer extends App {
  implicit val actorSystem: ActorSystem = ActorSystem("load-producer")

  val settings = SettingsLoader.load[LoadProducerSettings]("load-producer")

  val eventGenerator = new EventGenerator(settings.maxTopics)
  val ignoreInputMessage = (_: Message) => ""
  val generatorFlow = Flow.fromSinkAndSource(
    BroadcastHub.sink[String],
    Source
      .cycle(() => eventGenerator.generate(eventTemplate, topics, locations).toIterator)
      .throttle(settings.tps, 1.second)
  )

  val serverPushFlow =
    Flow[Message]
      .map(ignoreInputMessage)
      .via(generatorFlow)
      .map(str => TextMessage(str))

  val route = get {
    pathEndOrSingleSlash {
      handleWebSocketMessages(serverPushFlow)
    }
  }

  Http()
    .newServerAt(settings.host, settings.port)
    .bindFlow(route)

  println(s"Websocket open at ${settings.host}:${settings.port}.")
  println(s"Producing ${settings.tps} RSVPs per second with up to ${settings.maxTopics} topics.")
}
