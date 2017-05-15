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
  "com.amazonaws" % "aws-java-sdk" % "1.10.33",
  "com.amazonaws" % "amazon-kinesis-client" % "1.6.1",
  "com.gu" %% "pan-domain-auth-play_2-4-0" % "0.3.0",
  "com.gu" %% "editorial-permissions-client" % "0.2",
  ws, // for panda
  "org.cvogt" %% "play-json-extensions" % "0.6.0",
  "com.squareup.okhttp" % "okhttp" % "2.4.0",
  "org.apache.thrift" % "libthrift" % "0.8.0",
  "com.twitter" %% "scrooge-core" % "4.12.0",
  "com.google.guava" % "guava" % "18.0",
  "com.gu" %% "content-api-client" % "7.7",
  "com.gu" %% "tags-thrift-schema" % "1.0.0",
  "com.gu" %% "auditing-thrift-model" % "0.2",
  "net.logstash.logback" % "logstash-logback-encoder" % "4.2",
  "com.gu" % "kinesis-logback-appender" % "1.0.5",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.12"
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
    riffRaffBuildIdentifier := Option(System.getenv("CIRCLE_BUILD_NUM")).getOrElse("DEV"),
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffArtifactResources := Seq(
      riffRaffPackageType.value -> s"${name.value}/${name.value}.deb",
      baseDirectory.value / "cloudformation" / "tag-manager.yaml" -> "cloudformation/tag-manager.yaml"
    ),
    javaOptions in Universal ++= Seq(
      "-Dpidfile.path=/dev/null"
    ),

    debianPackageDependencies := Seq("openjdk-8-jre-headless"),
    maintainer := "digitial tools team <digitalcms.dev@guardian.co.uk>",
    packageSummary := "tag manager",
    packageDescription := """manage tags""",

    doc in Compile <<= target.map(_ / "none"),
    scalaVersion := "2.11.8",
    scalaVersion in ThisBuild := "2.11.8",
    libraryDependencies ++= dependencies
  )
  .settings(TagManager.settings: _*)
