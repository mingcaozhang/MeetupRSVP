package com.zhangmin.meetup.utils

import java.util.concurrent.atomic.AtomicLong

import com.zhangmin.meetup.model.Location
import io.circe.Json

import scala.util.Random

class EventGenerator(maxTopics: Int) {
  private val random: Random = new Random()
  private val idCounter: AtomicLong = new AtomicLong()

  /**
    * Generate an event with N random topics where N is between 1 and 10.
    */
  def generate(eventTemplate: Json, topics: List[String], locations: List[Location]): List[String] = {
    val randomTopics = getNRandomTopics(topics, random.nextInt(maxTopics) + 1)
      .map(topic => s""" { "urlkey": "$topic", "topic_name": "$topic" }""".stripMargin)
      .mkString(",")
    val location = getRandomLocation(locations)
    val topicsJson = io.circe.parser
      .parse(s"""
        | {
        |    "rsvp_id": ${idCounter.getAndIncrement()},
        |    "group": {
        |       "group_city": "${location.city}",
        |       "group_country": "${location.country}",
        |       "group_topics": [ $randomTopics ]
        |    },
        |    "event": {
        |       "time": ${System.currentTimeMillis()}
        |    }
        | }
        |""".stripMargin)
      .getOrElse(Json.Null)
    List(eventTemplate.deepMerge(topicsJson).toString)
  }

  private def getNRandomTopics(topics: List[String], n: Int): List[String] =
    (0 until n).map(_ => topics(random.nextInt(topics.size))).toList

  private def getRandomLocation(locations: List[Location]): Location = locations(random.nextInt(locations.size))

}
