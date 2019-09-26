val scala212 = "2.12.9"
val scala213 = "2.13.0"

scalaVersion in ThisBuild := scala213
scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

lazy val `play-mongo-bson` = (project in file("."))
  .settings(publish := Unit, publishLocal := Unit)
  .aggregate(lib, sample)

lazy val lib = (project in file("lib")).settings(
  name := """play-mongo-bson""",
  organization := "ai.snips",
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % "2.7.3" % Provided,
    "org.mongodb.scala" %% "mongo-scala-driver" % "2.7.0" % Provided,
    "org.scalatest" %% "scalatest" % "3.0.8" % Test,
    "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
  ),
  crossScalaVersions := Seq(scala213, scala212),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  credentials += Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    System.getenv("SONATYPE_USERNAME"),
    System.getenv("SONATYPE_PASSWORD")
  ),
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
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
)

lazy val sample = (project in file("sample")).enablePlugins(PlayScala).settings(
  name := """sample""",
  libraryDependencies ++= Seq(
    guice,
    "org.mongodb.scala" %% "mongo-scala-driver" % "2.7.0"
  ),
  publishArtifact := false
).dependsOn(lib)
