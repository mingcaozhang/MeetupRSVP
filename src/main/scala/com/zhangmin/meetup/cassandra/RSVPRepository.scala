package com.zhangmin.meetup.cassandra

import com.datastax.driver.core.SocketOptions
import com.outworkers.phantom.connectors.{CassandraConnection, ContactPoint}
import com.outworkers.phantom.database.Database
import com.zhangmin.meetup.model.RSVPDTO

import scala.concurrent.Future

class RSVPRepository(override val connector: CassandraConnection) extends Database[RSVPRepository](connector) {

  object RSVPs extends RSVPTable with connector.Connector

  def getAllInRange(dateString: String, fromMillis: Long, toMillis: Long): Future[Seq[RSVPDTO]] =
    RSVPs.getAllInRange(dateString, fromMillis, toMillis)
}

object RSVPRepository {
  def local(keyspace: String) = new RSVPRepository(
    ContactPoint.local
      .withClusterBuilder(
        _.withSocketOptions(
          new SocketOptions()
            .setConnectTimeoutMillis(20000)
            .setReadTimeoutMillis(20000)
        ).withoutJMXReporting()
      )
      .noHeartbeat()
      .keySpace(keyspace)
  )
}
