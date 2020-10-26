#!/bin/bash

if [[ $1 = 'cassandra' ]]; then
  # Create keyspace for single node cluster
  CQL_KEYSPACE="CREATE KEYSPACE meetup WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};"
  # Create table for RSVP events
  CQL_SCHEMA="USE meetup; CREATE TABLE rsvps(id BIGINT, day VARCHAR, time BIGINT, country VARCHAR, city VARCHAR, topics VARCHAR, PRIMARY KEY (day, time, id)) WITH CLUSTERING ORDER BY (time DESC, id DESC);"

  until echo "$CQL_KEYSPACE" | cqlsh; do
    echo "cqlsh: Cassandra is unavailable - retry later"
    sleep 2
  done &
  until echo "$CQL_SCHEMA" | cqlsh; do
    echo "cqlsh: Cassandra is unavailable - retry later"
    sleep 2
  done &
fi

exec /docker-entrypoint.sh "$@"