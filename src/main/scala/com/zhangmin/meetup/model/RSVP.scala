package com.zhangmin.meetup.model

final case class RSVP(rsvp_id: Long, event: Event, group: Group)
final case class Event(time: Long)
final case class Group(group_city: String, group_country: String, group_topics: List[Topic])
final case class Topic(topic_name: String, urlkey: String)
