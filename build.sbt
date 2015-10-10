name := "play-cassandra-evolutions"

organization := "de.leanovate"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-jdbc-evolutions" % "2.4.3",
  "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.8",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

fork in run := true
