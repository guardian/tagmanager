package model.jobs

import model.{Section, Tag}
import org.cvogt.play.json.Jsonx
import repositories._
import play.api.libs.json._
import services.{Config, KinesisStreams}

// Tags

case class ReindexTags() extends Step {
  override def process: Option[Step] = {
    TagLookupCache.allTags.get.grouped(Config().reindexTagsBatchSize).foreach { tags =>
      KinesisStreams.reindexTagsStream.publishUpdate("tagReindex", Tag.createReindexBatch(tags))
    }
    None
  }
}

object ReindexTags {
  implicit val reindexTagsFormat: Format[ReindexTags] = Jsonx.formatCaseClassUseDefaults[ReindexTags]
}

// Sections

case class ReindexSections() extends Step {
  override def process: Option[Step] = {
    SectionRepository.loadAllSections.foreach { section =>
      KinesisStreams.reindexSectionsStream.publishUpdate("sectionReindex", section.asThrift)
    }
    None
  }
}

object ReindexSections {
  implicit val reindexSectionsFormat: Format[ReindexSections] = Jsonx.formatCaseClassUseDefaults[ReindexSections]
}
