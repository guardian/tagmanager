package model.jobs

import com.gu.tagmanagement.OperationType
import model.{AppAudit, Tag, TagAudit}
import model.jobs.steps._
import repositories._

/** Utilities for starting jobs */
object JobHelper {
  def beginBatchTagAddition(tag: Tag, operation: String, contentIds: List[String]) {
    val section = tag.section.flatMap( SectionRepository.getSection(_) )
    val top: Boolean = operation == OperationType.AddToTop

    JobRepository.addJob(
      Job(
        Sequences.jobId.getNextId,
        List(AddTagToContent(tag, section, contentIds, top))
        )
      )

    TagAuditRepository.upsertTagAudit(TagAudit.batchTag(tag, operation, contentIds.length))
  }

  def beginBatchTagDeletion(tag: Tag, operation: String, contentIds: List[String]) {
    val section = tag.section.flatMap( SectionRepository.getSection(_) )

    JobRepository.addJob(
      Job(
        Sequences.jobId.getNextId,
        List(RemoveTagFromContent(tag, section, contentIds))
        )
      )

    TagAuditRepository.upsertTagAudit(TagAudit.batchTag(tag, operation, contentIds.length))
  }

  def beginTagReindex() = {
    val expectedDocs = TagLookupCache.allTags.get().size
    ReindexProgressRepository.resetTagReindexProgress(expectedDocs)

    JobRepository.addJob(
      Job(
        Sequences.jobId.getNextId,
        List(ReindexTags())
        )
      )
    AppAuditRepository.upsertAppAudit(AppAudit.reindexTags);
  }

  def beginSectionReindex() = {
    ReindexProgressRepository.resetSectionReindexProgress(SectionRepository.count)

    JobRepository.addJob(
      Job(
        Sequences.jobId.getNextId,
        List(ReindexSections())
        )
      )
    AppAuditRepository.upsertAppAudit(AppAudit.reindexSections);
  }

  def beginMergeTag(from: Tag, to: Tag) = {
    val fromSection = from.section.flatMap( SectionRepository.getSection(_) )
    val toSection = to.section.flatMap( SectionRepository.getSection(_) )
    val contentIds = ContentAPI.getContentIdsForTag(from.path)

    JobRepository.addJob(
      Job(
        Sequences.jobId.getNextId,
        List(
          MergeTagForContent(from, to, fromSection, toSection, contentIds)
          ) ++ removeTagSteps(from)
        )
      )
  }

  def beginTagDeletion(tag: Tag) = {
    val section = tag.section.flatMap( SectionRepository.getSection(_))
    val contentIds = ContentAPI.getContentIdsForTag(tag.path)

    JobRepository.addJob(
      Job(
        Sequences.jobId.getNextId,
        List(
          RemoveTagFromContent(tag, section, contentIds)
          ) ++ removeTagSteps(tag)
        )
      )
  }

  private def removeTagSteps(tag: Tag) = List(RemoveTagPath(tag), RemoveTagFromCapi(tag), RemoveTag(tag))
}
