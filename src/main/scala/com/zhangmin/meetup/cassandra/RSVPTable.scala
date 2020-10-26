package com.zhangmin.meetup.cassandra

import com.outworkers.phantom.Table
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.keys.{ClusteringOrder, PartitionKey}
import com.zhangmin.meetup.model.RSVPDTO

import scala.concurrent.Future

abstract class RSVPTable extends Table[RSVPTable, RSVPDTO] {
  override val tableName = "rsvps"

  object day extends StringColumn with PartitionKey
  object time extends LongColumn with ClusteringOrder
  object id extends LongColumn with ClusteringOrder
  object city extends StringColumn
  object country extends StringColumn
  object topics extends StringColumn

  lazy val preparedGetAllInRange =
    select
      .where(_.day in ?)
      .and(_.time > ?)
      .and(_.time <= ?)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .prepare()

  def getAllInRange(day: String, fromMillis: Long, toMillis: Long): Future[Seq[RSVPDTO]] =
    preparedGetAllInRange.bind(ListValue(day), fromMillis, toMillis).fetch()
}
