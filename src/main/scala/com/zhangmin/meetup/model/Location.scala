package com.zhangmin.meetup.model

final case class Location(country: String, city: String)
final case class CountryInfo(name: String, cities: List[String])
