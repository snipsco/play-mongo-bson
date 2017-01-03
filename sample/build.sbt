name := """sample"""

version := "1.0-SNAPSHOT"

lazy val sample = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

resolvers ++= Seq(
  "Snips Artifactory Snapshots" at "http://artifactory.corp.snips.net/artifactory/libs-snapshot-local",
  "Snips Artifactory Releases" at "http://artifactory.corp.snips.net/artifactory/libs-release-local"
)

libraryDependencies ++= Seq(
  "ai.snips" %% "play-mongo-bson" % "1.0",
  "org.mongodb" % "mongo-java-driver" % "3.2.2",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1"
)