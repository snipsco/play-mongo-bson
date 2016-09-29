name := """sample"""

version := "1.0-SNAPSHOT"

lazy val sample = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "ai.snips" %% "play-mongo-bson" % "0.1-SNAPSHOT",
  "org.mongodb" % "mongo-java-driver" % "3.2.2" % "provided",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1"
)

