package com.zhangmin.meetup.utils

import com.zhangmin.meetup.model.{CountryInfo, Location}
import io.circe.generic.auto._
import io.circe.{Json, parser}

import scala.io.Source

object Resources {
  val eventTemplate: Json =
    parser.parse(Source.fromResource("event.json").getLines().mkString("")).getOrElse(Json.Null)
  val topics: List[String] = Source.fromResource("topics.txt").getLines().drop(1).toList
  val locations: List[Location] =
    parser
      .decode[List[CountryInfo]](Source.fromResource("countries.json").getLines().mkString("\n"))
      .getOrElse(List.empty)
      .flatMap(countryInfo => countryInfo.cities.map(city => Location(countryInfo.name, city)))
}
