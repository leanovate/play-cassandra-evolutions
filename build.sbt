import ReleaseTransformations._

name := "play-cassandra-evolutions"

organization := "de.leanovate"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-jdbc-evolutions" % "2.4.3",
  "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.8",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.mockito" % "mockito-core" % "1.10.19" % "test"
)

fork in run := true

publishMavenStyle := true

pomExtra := {
  <url>https://github.com/leanovate/toehold</url>
    <licenses>
      <license>
        <name>MIT</name>
        <url>http://opensource.org/licenses/MIT</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:github.com/leanovate/play-cassandra-evolutions</connection>
      <developerConnection>scm:git:git@github.com:/leanovate/play-cassandra-evolutions</developerConnection>
      <url>github.com/leanovate/play-cassandra-evolutions</url>
    </scm>
    <developers>
      <developer>
        <id>untoldwind</id>
        <name>Bodo Junglas</name>
        <url>http://untoldwind.github.io/</url>
      </developer>
    </developers>
}

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _)),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
  pushChanges
)