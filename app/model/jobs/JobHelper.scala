package model.jobs

import com.gu.tagmanagement.OperationType
import model.{AppAudit, Tag, TagAudit}
import model.jobs.steps._
import repositories._

/** Utilities for starting jobs */
object JobHelper {
  def beginBatchTagAddition(tag: Tag, operation: String, contentIds: List[String])(implicit username: Option[String]) {
    val section = tag.section.flatMap( SectionRepository.getSection(_) )

    JobRepository.addJob(
      Job(
        id = Sequences.jobId.getNextId,
        title = s"Batch tag add '${tag.path}'",
        createdBy = username,
        steps = List(AddTagToContent(tag, section, contentIds, operation))
        )
      )

    TagAuditRepository.upsertTagAudit(TagAudit.batchTag(tag, operation, contentIds.length))
  }

  def beginBatchTagDeletion(tag: Tag, operation: String, contentIds: List[String])(implicit username: Option[String]) {
    val section = tag.section.flatMap( SectionRepository.getSection(_) )

    JobRepository.addJob(
      Job(
        id = Sequences.jobId.getNextId,
        title = s"Batch tag remove '${tag.path}'",
        createdBy = username,
        steps = List(RemoveTagFromContent(tag, section, contentIds)),
        tagIds = List(tag.id)
        )
      )

    TagAuditRepository.upsertTagAudit(TagAudit.batchTag(tag, operation, contentIds.length))
  }

  def beginTagReindex()(implicit username: Option[String]) = {
    val expectedDocs = TagLookupCache.allTags.get().size
    ReindexProgressRepository.resetTagReindexProgress(expectedDocs)

    JobRepository.addJob(
      Job(
        id = Sequences.jobId.getNextId,
        title = "Tag reindex",
        createdBy = username,
        steps = List(ReindexTags())
        )
      )
    AppAuditRepository.upsertAppAudit(AppAudit.reindexTags);
  }

  def beginSectionReindex()(implicit username: Option[String]) = {
    ReindexProgressRepository.resetSectionReindexProgress(SectionRepository.count)

    JobRepository.addJob(
      Job(
        id = Sequences.jobId.getNextId,
        title = "Section reindex",
        createdBy = username,
        steps = List(ReindexSections())
        )
      )
    AppAuditRepository.upsertAppAudit(AppAudit.reindexSections);
  }

  def beginMergeTag(from: Tag, to: Tag)(implicit username: Option[String]) = {
    val fromSection = from.section.flatMap( SectionRepository.getSection(_) )
    val toSection = to.section.flatMap( SectionRepository.getSection(_) )

    JobRepository.addJob(
      Job(
        id = Sequences.jobId.getNextId,
        title = s"Merging from '${from.path}' to '${to.path}'",
        createdBy = username,
        steps = List(
          MergeTagForContent(from, to, fromSection, toSection)
          ) ++ removeTagSteps(from),
        tagIds = List(from.id, to.id)
        )
      )
  }

  def beginTagDeletion(tag: Tag)(implicit username: Option[String]) = {
    val section = tag.section.flatMap( SectionRepository.getSection(_))
    val contentIds = ContentAPI.getContentIdsForTag(tag.path)

    JobRepository.addJob(
      Job(
        id = Sequences.jobId.getNextId,
        title = s"Deleting '${tag.path}'",
        createdBy = username,
        steps = List(
          RemoveTagFromContent(tag, section, contentIds)
          ) ++ removeTagSteps(tag),
        tagIds = List(tag.id)
        )
      )
  }

  private def removeTagSteps(tag: Tag) = List(RemoveTagPath(tag), RemoveTagFromCapi(tag), RemoveTag(tag))
}
