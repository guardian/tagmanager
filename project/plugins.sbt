// Use the Play sbt plugin for Play projects
addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.5")

addSbtPlugin("com.github.sbt" % "sbt-digest" % "2.0.0")

addSbtPlugin("com.github.sbt" % "sbt-gzip" % "2.0.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.12.0")

// need to add jdeb dependency explicitly because https://github.com/sbt/sbt-native-packager/issues/1053
libraryDependencies += "org.vafer" % "jdeb" % "1.3" artifacts (Artifact("jdeb", "jar", "jar"))

/*
   See https://github.com/guardian/maintaining-scala-projects/issues/13
 */
libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
