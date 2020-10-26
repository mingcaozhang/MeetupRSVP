package com.zhangmin.meetup

import com.datastax.spark.connector.streaming._
import com.zhangmin.meetup.config.{RSVPStreamerSettings, SettingsLoader}
import com.zhangmin.meetup.model.{RSVP, RSVPDTO}
import com.zhangmin.meetup.utils.CustomCodecs._
import com.zhangmin.meetup.utils.WebSocketReceiver
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import pureconfig.generic.auto._

/**
  * App to stream websocket RSVP events into a Cassandra cluster.
  * Unused fields in the event payload are discarded for brevity.
  */
object RSVPStreamer extends App {
  val settings: RSVPStreamerSettings = SettingsLoader.load[RSVPStreamerSettings]("rsvp-streamer")
  val sparkConf =
    new SparkConf(true)
      .setAppName("RSVP Streamer")
      .setMaster(settings.spark.master)
      .set("spark.cassandra.connection.host", settings.cassandra.host)
  val ssc = new StreamingContext(sparkConf, Seconds(1))

  ssc.checkpoint(settings.spark.checkpointDir)
  ssc
    .receiverStream(new WebSocketReceiver[RSVP](settings.spark.websocketUrl))
    .map(RSVPDTO.fromRSVP)
    .saveToCassandra(settings.cassandra.keyspace, settings.cassandra.table)
  ssc.start()
  ssc.awaitTermination()
}
