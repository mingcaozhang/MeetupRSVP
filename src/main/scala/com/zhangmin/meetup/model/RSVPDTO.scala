package com.zhangmin.meetup.model

import com.zhangmin.meetup.utils.DateTimeUtils

final case class RSVPDTO(day: String, time: Long, id: Long, city: String, country: String, topics: String)

object RSVPDTO {
  def fromRSVP(rsvp: RSVP): RSVPDTO =
    RSVPDTO(
      DateTimeUtils.toDateString(rsvp.event.time),
      rsvp.event.time,
      rsvp.rsvp_id,
      rsvp.group.group_city,
      rsvp.group.group_country,
      rsvp.group.group_topics.map(_.topic_name).mkString(",")
    )
}
