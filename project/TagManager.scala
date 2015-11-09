import com.typesafe.sbt.web.PathMapping
import com.typesafe.sbt.web.pipeline.Pipeline
import sbt._
import sbt.Keys._

import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.web.Import.WebKeys._


/**
 * Front-end specific settings for Tag Manager in SBT.
 */
object TagManager {
  val gzipSettings = Seq(includeFilter in gzip := "*.html" || "*.css" || "*.js")

  val assetPipelineSettings = Seq(pipelineStages := Seq(digest, gzip))

  val settings = gzipSettings ++ assetPipelineSettings
}
