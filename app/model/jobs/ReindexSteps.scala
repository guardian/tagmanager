package model.jobs

import model.{Section, Tag}
import org.cvogt.play.json.Jsonx
import repositories._
import play.api.libs.json._
import services.{Config, KinesisStreams}

case class ReindexTags() extends Step {
  override def process: Option[Step] = {
    val total = TagLookupCache.allTags.get.size
    var progress: Int = 0

    TagLookupCache.allTags.get.grouped(Config().reindexTagsBatchSize).foreach { tags =>
      KinesisStreams.reindexTagsStream.publishUpdate("tagReindex", Tag.createReindexBatch(tags))

      progress += tags.size
      ReindexProgressRepository.updateTagReindexProgress(progress, total)
    }
    ReindexProgressRepository.completeTagReindex(progress, total)
    None
  }
}

object ReindexTags {
  implicit val reindexTagsFormat: Format[ReindexTags] = Jsonx.formatCaseClassUseDefaults[ReindexTags]
}

case class ReindexSections() extends Step {
  override def process: Option[Step] = {
    val sections = SectionRepository.loadAllSections.toList
    val total = sections.size
    var progress: Int = 0

    sections.foreach { section =>
      KinesisStreams.reindexSectionsStream.publishUpdate("sectionReindex", section.asThrift)

      progress += 1
      ReindexProgressRepository.updateSectionReindexProgress(progress, total)
    }
    ReindexProgressRepository.completeSectionReindex(progress, total)
    None
  }
}

object ReindexSections {
  implicit val reindexSectionsFormat: Format[ReindexSections] = Jsonx.formatCaseClassUseDefaults[ReindexSections]
}
