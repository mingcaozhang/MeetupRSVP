package com.zhangmin.meetup

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import com.zhangmin.meetup.api.TopkService
import com.zhangmin.meetup.config.{SettingsLoader, TrendingTopicsSettings}
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext

/**
  * App to query Cassandra and display trending topics.
  */
object TrendingTopics extends App {
  implicit val actorSystem: ActorSystem = ActorSystem("trending-topics")
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val settings = SettingsLoader.load[TrendingTopicsSettings]("trending-topics")
  val topkService = new TopkService(settings)

  val route = path("topk") {
    get {
      parameters('until.?, 'interval.as[Int].?, 'count.as[Int].?, 'city.?, 'country.?) {
        (
          until: Option[String],
          interval: Option[Int],
          count: Option[Int],
          city: Option[String],
          country: Option[String]
        ) =>
          complete(topkService.handleRequest(until, interval, count, city, country))
      }
    }
  }

  Http()
    .newServerAt(settings.host, settings.port)
    .bindFlow(route)

  println(s"Server listening at ${settings.host}:${settings.port}.")
}
