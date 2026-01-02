import play.sbt.PlayImport.PlayKeys._

name := "tag-manager"

version := "1.0"

lazy val scalaVer = "2.13.18"

resolvers ++= Resolver.sonatypeOssRepos("releases")

scalacOptions ++= Seq(
  "-quickfix:any",
  "-release:11",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation",
  "-feature"
)

lazy val awsVersion = "1.12.403"

val pandaVersion = "9.0.0"
lazy val dependencies = Seq(
  "com.amazonaws" % "aws-java-sdk-dynamodb" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-ec2" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-kinesis" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-s3" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-sqs" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-sts" % awsVersion,
  "com.amazonaws" % "amazon-kinesis-client" % "1.15.3",
  "com.gu" %% "pan-domain-auth-play_3-0" % pandaVersion,
  "com.gu" %% "editorial-permissions-client" % "2.15",
  ws, // for panda
  "ai.x" %% "play-json-extensions" % "0.42.0",
  "com.squareup.okhttp3" % "okhttp" % "4.9.2",
  "com.google.guava" % "guava" % "18.0",
  "com.gu" %% "content-api-client-default" % "27.0.0",
  "com.gu" %% "tags-thrift-schema" % "2.8.5",
  "net.logstash.logback" % "logstash-logback-encoder" % "7.2",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.12",
  "com.gu"  %% "panda-hmac-play_3-0" % pandaVersion,
  "com.gu" %% "content-api-client-aws" % "0.7.4",
  "com.beachape" %% "enumeratum" % "1.5.13",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "com.typesafe.play" %% "play-json-joda" % "2.8.1",
  "org.apache.commons" % "commons-lang3" % "3.11",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.18.2",
  "com.madgag" %% "scala-collection-plus" % "0.11"
)

dependencyOverrides += "org.bouncycastle" % "bcprov-jdk15on" % "1.67"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb, JDebPackaging, SystemdPlugin, BuildInfoPlugin)
  .settings(Defaults.coreDefaultSettings: _*)
  .settings(
    playDefaultPort := 8247,
    Universal / name := normalizedName.value,
    topLevelDirectory := Some(normalizedName.value),
    Universal / javaOptions ++= Seq(
      "-Dpidfile.path=/dev/null"
    ),

    debianPackageDependencies := Seq("java11-runtime-headless"),
    maintainer := "digital tools team <digitalcms.dev@guardian.co.uk>",
    packageSummary := "tag manager",
    packageDescription := """manage tags""",

    Compile / doc := (target.value / "none"),
    scalaVersion := scalaVer,
    ThisBuild / scalaVersion := scalaVer,
    libraryDependencies ++= dependencies,
    buildInfoKeys := Seq(
      name,
      BuildInfoKey.sbtbuildinfoConstantEntry("buildTime", System.currentTimeMillis),
      "gitCommitId" -> Option(System.getenv("GITHUB_SHA")).getOrElse("Unknown")
    ),
    buildInfoPackage := "com.gu.tagmanager",
    buildInfoOptions := Seq(BuildInfoOption.ToJson),

    gzip / includeFilter := "*.html" || "*.css" || "*.js",
    pipelineStages := Seq(digest, gzip),
  )
