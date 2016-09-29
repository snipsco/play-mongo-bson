name := """play-mongo-bson"""

version := "0.1-SNAPSHOT"

organization := "ai.snips"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.5.8" % "provided",
  "org.mongodb" % "mongo-java-driver" % "3.2.2" % "provided",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

// TODO: publish artifactory

