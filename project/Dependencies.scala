import sbt._

object Dependencies {

  val AkkaVersion = "2.6.8"
  val AkkaHttpVersion = "10.2.0"
  val CirceVersion = "0.12.3"
  val JodaTimeVersion = "2.10.8"
  val PhantomVersion = "2.59.0"
  val PureConfigVersion = "0.14.0"
  val ScalaWebsocketVersion = "0.2.1"
  val SparkCassandraConnectorVersion = "3.0.0"
  val SparkVersion = "3.0.1"

  lazy val deps: Seq[ModuleID] = Seq(
    "com.datastax.spark" %% "spark-cassandra-connector" % SparkCassandraConnectorVersion,
    "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
    "com.outworkers" %% "phantom-dsl" % PhantomVersion,
    "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "io.circe" %% "circe-core" % CirceVersion,
    "io.circe" %% "circe-generic" % CirceVersion,
    "io.circe" %% "circe-parser" % CirceVersion,
    "io.dropwizard.metrics" % "metrics-core" % "3.2.2",
    "joda-time" % "joda-time" % JodaTimeVersion,
    "org.apache.spark" %% "spark-core" % SparkVersion,
    "org.apache.spark" %% "spark-sql" % SparkVersion,
    "org.apache.spark" %% "spark-streaming" % SparkVersion,
    "org.saegesser" %% "scalawebsocket" % ScalaWebsocketVersion
  ).map(_.exclude("org.log4j", "*")) ++ Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.3"
  )
}
