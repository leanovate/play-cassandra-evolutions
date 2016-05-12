name := "play-cassandra-evolutions-example"

organization := "de.leanovate"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "com.typesafe.play" %% "play-jdbc-evolutions" % "2.5.3",
  "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.9",
  "de.leanovate" %% "play-cassandra-evolutions" % "2.5.1-SNAPSHOT"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
