name := """play-mongo-bson"""

organization := "ai.snips"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.6.13" % "provided",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.3.0" % "provided",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

scalacOptions ++= Seq("-unchecked", "-deprecation")

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  System.getenv("SONATYPE_USERNAME"),
  System.getenv("SONATYPE_PASSWORD"))

releasePublishArtifactsAction := PgpKeys.publishSigned.value

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
        <url>https://snips.ai</url>
      </developer>
    </developers>
