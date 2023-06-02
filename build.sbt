import play.sbt.PlayImport.PlayKeys._

name := "tag-manager"

version := "1.0"

lazy val scalaVer = "2.12.16"

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation",
  "-feature"
)

lazy val awsVersion = "1.12.403"

lazy val dependencies = Seq(
  "com.amazonaws" % "aws-java-sdk-dynamodb" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-ec2" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-kinesis" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-s3" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-sqs" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-sts" % awsVersion,
  "com.amazonaws" % "amazon-kinesis-client" % "1.14.10",
  "com.gu" %% "pan-domain-auth-play_2-8" % "1.2.3",
  "com.gu" %% "editorial-permissions-client" % "0.9",
  ws, // for panda
  "ai.x" %% "play-json-extensions" % "0.42.0",
  "com.squareup.okhttp3" % "okhttp" % "4.9.2",
  "com.google.guava" % "guava" % "18.0",
  "com.gu" %% "content-api-client-default" % "17.24.1",
  "com.gu" %% "tags-thrift-schema" % "2.8.1",
  "net.logstash.logback" % "logstash-logback-encoder" % "7.2",
  "com.gu" % "kinesis-logback-appender" % "1.0.5",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.12",
  "com.gu"  %% "panda-hmac-play_2-8" % "2.0.1",
  "com.gu" %% "content-api-client-aws" % "0.5",
  "com.beachape" %% "enumeratum" % "1.5.13",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.typesafe.play" %% "play-json-joda" % "2.8.1",
  "org.apache.commons" % "commons-lang3" % "3.11",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.2",
)

dependencyOverrides += "org.bouncycastle" % "bcprov-jdk15on" % "1.67"

lazy val root = (project in file(".")).enablePlugins(PlayScala, RiffRaffArtifact, SbtWeb, JDebPackaging, SystemdPlugin)
  .settings(Defaults.coreDefaultSettings: _*)
  .settings(
    playDefaultPort := 8247,
    Universal / name := normalizedName.value,
    topLevelDirectory := Some(normalizedName.value),
    riffRaffPackageType := (Debian / packageBin).value,
    riffRaffManifestProjectName := s"editorial-tools:${name.value}",
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffArtifactResources := Seq(
      baseDirectory.value / "riff-raff.yaml" -> "riff-raff.yaml",
      riffRaffPackageType.value -> s"${name.value}/${name.value}.deb",
      baseDirectory.value / "cloudformation" / "tag-manager.yaml" -> "cloudformation/tag-manager.yaml"
    ),
    Universal / javaOptions ++= Seq(
      "-Dpidfile.path=/dev/null"
    ),

    debianPackageDependencies := Seq("openjdk-8-jre-headless"),
    maintainer := "digital tools team <digitalcms.dev@guardian.co.uk>",
    packageSummary := "tag manager",
    packageDescription := """manage tags""",

    Compile / doc := (target.value / "none"),
    scalaVersion := scalaVer,
    ThisBuild / scalaVersion := scalaVer,
    libraryDependencies ++= dependencies,

    gzip / includeFilter := "*.html" || "*.css" || "*.js",
    pipelineStages := Seq(digest, gzip),
  )
