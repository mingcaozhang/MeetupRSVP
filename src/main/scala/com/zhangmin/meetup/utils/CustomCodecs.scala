package com.zhangmin.meetup.utils

import com.zhangmin.meetup.model._
import io.circe._
import io.circe.generic.semiauto._

object CustomCodecs {
  implicit val rsvpDecoder: Decoder[RSVP] = deriveDecoder[RSVP]
  implicit val eventDecoder: Decoder[Event] = deriveDecoder[Event]
  implicit val groupDecoder: Decoder[Group] = deriveDecoder[Group]
  implicit val topicDecoder: Decoder[Topic] = deriveDecoder[Topic]
}
