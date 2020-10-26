package com.zhangmin.meetup.config

final case class RSVPStreamerSettings(cassandra: CassandraConfigs, spark: SparkConfigs)
final case class CassandraConfigs(host: String, port: Int, keyspace: String, table: String)
final case class SparkConfigs(checkpointDir: String, master: String, websocketUrl: String)

final case class LoadProducerSettings(host: String, port: Int, tps: Int, maxTopics: Int)

final case class TrendingTopicsSettings(
  host: String,
  port: Int,
  cassandra: CassandraConfigs,
  topkService: TopkServiceConfigs
)
final case class TopkServiceConfigs(defaultTopCount: Int, defaultInterval: Int)
