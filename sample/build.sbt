name := """sample"""

version := "1.0"

lazy val sample = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

resolvers += "Sonatype OSS" at "https://oss.sonatype.org/content/groups/public"

libraryDependencies ++= Seq(
  guice,
  "ai.snips" %% "play-mongo-bson" % "0.5-SNAPSHOT",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0"
)
