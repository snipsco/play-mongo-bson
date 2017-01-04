name := """sample"""

version := "1.0"

lazy val sample = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

resolvers += "Sonatype OSS" at "https://oss.sonatype.org/content/groups/public"

libraryDependencies ++= Seq(
  "ai.snips" %% "play-mongo-bson" % "0.2-SNAPSHOT",
  "org.mongodb" % "mongo-java-driver" % "3.2.2",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1"
)