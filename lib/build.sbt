name := """play-mongo-bson"""

organization := "ai.snips"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.5.8" % "provided",
  "org.mongodb" % "mongo-java-driver" % "3.2.2" % "provided",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

/*
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
*/

publishTo := {
  val nexus = "http://artifactory.corp.snips.net/artifactory/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "libs-snapshot-local")
  else
    Some("releases"  at nexus + "libs-release-local")
}

credentials += Credentials(
  "Artifactory Realm",
  "artifactory.corp.snips.net",
  System.getenv("ARTIFACTORY_USERNAME"),
  System.getenv("ARTIFACTORY_PASSWORD"))

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra :=
  <url>https://snips.ai</url>
    <licenses>
      <license>
        <name>MIT</name>
        <url>https://opensource.org/licenses/MIT</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:snipsco/play-mongo-bson.git</url>
      <connection>scm:git:git@github.com:snipsco/play-mongo-bson.git</connection>
    </scm>
    <developers>
      <developer>
        <id>kali</id>
        <name>Mathieu Poumeyrol</name>
        <url>https://snips.ai</url>
      </developer>
      <developer>
        <id>gstraymond</id>
        <name>Guillaume Saint-Raymond</name>
        <url>http://www.eigengo.com</url>
      </developer>
    </developers>
