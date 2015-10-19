addCommandAlias("dist", ";riffRaffArtifact")

import play.PlayImport.PlayKeys._

name := "tag-manager"

version := "1.0"

lazy val dependencies = Seq(
  "com.amazonaws" % "aws-java-sdk" % "1.10.6",
  "net.logstash.logback" % "logstash-logback-encoder" % "4.2",
  "com.gu" % "kinesis-logback-appender" % "1.0.5",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.12"
)


lazy val root = (project in file(".")).enablePlugins(PlayScala, RiffRaffArtifact)
  .settings(Defaults.coreDefaultSettings: _*)
  .settings(
    playDefaultPort := 8247,
    packageName in Universal := normalizedName.value,
    riffRaffPackageType := (packageZipTarball in config("universal")).value,
    doc in Compile <<= target.map(_ / "none"),
    scalaVersion := "2.11.7",
    scalaVersion in ThisBuild := "2.11.7",
    libraryDependencies ++= dependencies
  )
