addCommandAlias("dist", ";riffRaffArtifact")

import play.sbt.PlayImport.PlayKeys._

name := "tag-manager"

version := "1.0"

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation",
  "-feature"
)

resolvers += "Guardian Bintray" at "https://dl.bintray.com/guardian/editorial-tools"

lazy val dependencies = Seq(
  "com.amazonaws" % "aws-java-sdk" % "1.11.678",
  "com.amazonaws" % "amazon-kinesis-client" % "1.8.9",
  "com.gu" %% "pan-domain-auth-play_2-4-0" % "0.3.0",
  "com.gu" %% "editorial-permissions-client" % "0.2",
  ws, // for panda
  "org.cvogt" %% "play-json-extensions" % "0.6.0",
  "com.squareup.okhttp3" % "okhttp" % "3.9.0",
  "com.google.guava" % "guava" % "18.0",
  "com.gu" %% "content-api-client-default" % "14.2",
  "com.gu" %% "tags-thrift-schema" % "2.7.0",
  "net.logstash.logback" % "logstash-logback-encoder" % "4.2",
  "com.gu" % "kinesis-logback-appender" % "1.0.5",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.12",
  "com.gu"  %% "panda-hmac" % "1.3.0",
  "com.gu" %% "content-api-client-aws" % "0.5",
  "com.beachape" %% "enumeratum" % "1.5.13",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

import com.typesafe.sbt.packager.archetypes.ServerLoader.Systemd
serverLoading in Debian := Systemd

lazy val root = (project in file(".")).enablePlugins(PlayScala, RiffRaffArtifact, SbtWeb, JDebPackaging)
  .settings(Defaults.coreDefaultSettings: _*)
  .settings(
    playDefaultPort := 8247,
    name in Universal := normalizedName.value,
    topLevelDirectory := Some(normalizedName.value),
    riffRaffPackageType := (packageBin in Debian).value,
    riffRaffPackageName := name.value,
    riffRaffManifestProjectName := s"editorial-tools:${name.value}",
    riffRaffBuildIdentifier := Option(System.getenv("BUILD_NUMBER")).getOrElse("DEV"),
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffArtifactResources := Seq(
      baseDirectory.value / "riff-raff.yaml" -> "riff-raff.yaml",
      riffRaffPackageType.value -> s"${name.value}/${name.value}.deb",
      baseDirectory.value / "cloudformation" / "tag-manager.yaml" -> "cloudformation/tag-manager.yaml"
    ),
    javaOptions in Universal ++= Seq(
      "-Dpidfile.path=/dev/null"
    ),

    debianPackageDependencies := Seq("openjdk-8-jre-headless"),
    maintainer := "digital tools team <digitalcms.dev@guardian.co.uk>",
    packageSummary := "tag manager",
    packageDescription := """manage tags""",

    doc in Compile <<= target.map(_ / "none"),
    scalaVersion := "2.11.12",
    scalaVersion in ThisBuild := "2.11.8",
    libraryDependencies ++= dependencies
  )
  .settings(TagManager.settings: _*)
