package com.zhangmin.meetup.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date

object DateTimeUtils {

  private val simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")

  def formatDateString(date: Date): String = simpleDateFormat.format(date)
  def toDateString(epochMillis: Long): String = formatDateString(Date.from(Instant.ofEpochMilli(epochMillis)))
}
