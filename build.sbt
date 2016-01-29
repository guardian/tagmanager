addCommandAlias("dist", ";riffRaffArtifact")

import play.PlayImport.PlayKeys._

name := "tag-manager"

version := "1.0"

lazy val dependencies = Seq(
  "com.amazonaws" % "aws-java-sdk" % "1.10.33",
  "com.amazonaws" % "amazon-kinesis-client" % "1.6.1",
  "com.gu" %% "pan-domain-auth-play_2-4-0" % "0.2.8",
  "com.gu" %% "editorial-permissions-client" % "0.2",
  ws, // for panda
  "com.squareup.okhttp" % "okhttp" % "2.4.0",
  "org.apache.thrift" % "libthrift" % "0.8.0",
  "com.twitter" %% "scrooge-core" % "4.1.0",
  "com.google.guava" % "guava" % "18.0",
  "com.gu" %% "content-api-client" % "7.7",
  "com.gu" %% "tags-thrift-schema" % "0.3.7",
  "net.logstash.logback" % "logstash-logback-encoder" % "4.2",
  "com.gu" % "kinesis-logback-appender" % "1.0.5",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.12"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala, RiffRaffArtifact, SbtWeb)
  .settings(Defaults.coreDefaultSettings: _*)
  .settings(
    playDefaultPort := 8247,
    packageName in Universal := normalizedName.value,
    riffRaffPackageType := (packageZipTarball in config("universal")).value,
    riffRaffPackageName := s"editorial-tools:${name.value}",
    riffRaffManifestProjectName := riffRaffPackageName.value,
    riffRaffBuildIdentifier := Option(System.getenv("CIRCLE_BUILD_NUM")).getOrElse("DEV"),
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffArtifactResources := Seq(
      riffRaffPackageType.value -> s"packages/${name.value}/${riffRaffPackageType.value.getName}",
      baseDirectory.value / "deploy.json" -> "deploy.json",
      baseDirectory.value / "cloudformation" / "tag-manager.json" ->
        "packages/cloudformation/tag-manager.json"
    ),
    doc in Compile <<= target.map(_ / "none"),
    scalaVersion := "2.11.7",
    scalaVersion in ThisBuild := "2.11.7",
    libraryDependencies ++= dependencies
  )
  .settings(TagManager.settings: _*)
