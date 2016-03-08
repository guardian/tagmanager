package model.jobs

import com.gu.tagmanagement.OperationType
import model.{AppAudit, Tag, TagAudit}
import model.jobs.steps._
import org.joda.time.{DateTime, DateTimeZone}
import repositories._

/** Utilities for starting jobs */
object JobHelper {
  def beginBatchTagAddition(tag: Tag, operation: String, contentIds: List[String]) {
    val section = tag.section.flatMap( SectionRepository.getSection(_) )
    val top: Boolean = operation == OperationType.AddToTop
    // TODO implement
    TagAuditRepository.upsertTagAudit(TagAudit.batchTag(tag, operation, contentIds.length))
  }

  def beginBatchTagDeletion(tag: Tag, operation: String, contentIds: List[String]) {
    val section = tag.section.flatMap( SectionRepository.getSection(_) )
    // TODO implement
    TagAuditRepository.upsertTagAudit(TagAudit.batchTag(tag, operation, contentIds.length))
  }

  def beginTagReindex() = {
    val expectedDocs = TagLookupCache.allTags.get().size
    ReindexProgressRepository.resetTagReindexProgress(expectedDocs)
    JobRepository.addJob(
      Job(
        Sequences.jobId.getNextId,
        new DateTime().getMillis,
        JobType.reindexTags,
        JobStatus.waiting,
        None,
        List(ReindexTags()),
        0,
        new DateTime(DateTimeZone.UTC).getMillis
        )
      )
    AppAuditRepository.upsertAppAudit(AppAudit.reindexTags);
  }

  def beginSectionReindex() = {
    ReindexProgressRepository.resetSectionReindexProgress(SectionRepository.count)
    JobRepository.addJob(
      Job(
        Sequences.jobId.getNextId,
        new DateTime().getMillis,
        JobType.reindexSections,
        JobStatus.waiting,
        None,
        List(ReindexSections()),
        0,
        new DateTime(DateTimeZone.UTC).getMillis
        )
      )
    AppAuditRepository.upsertAppAudit(AppAudit.reindexSections);
  }

  def beginMergeTag(from: Tag, to: Tag) = {
    val removingTagSection = from.section.flatMap( SectionRepository.getSection(_) )
    val replacementTagSection = to.section.flatMap( SectionRepository.getSection(_) )
    JobRepository.addJob(
      Job(
        Sequences.jobId.getNextId,
        new DateTime().getMillis,
        JobType.merge,
        JobStatus.waiting,
        None,
        List(
          // TODO implement
          ),
        0,
        new DateTime(DateTimeZone.UTC).getMillis
        )
      )
  }

  def beginTagDeletion(tag: Tag) = {
    val section = tag.section.flatMap( SectionRepository.getSection(_))
    val contentIds = ContentAPI.getContentIdsForTag(tag.path)

    JobRepository.addJob(
      Job(
        Sequences.jobId.getNextId,
        new DateTime().getMillis,
        JobType.delete,
        JobStatus.waiting,
        None,
        List(
          RemoveTagFromContent(tag, section, contentIds),
          RemoveTagPath(tag),
          RemoveTagFromCapi(tag),
          RemoveTag(tag)
          ),
        0,
        new DateTime(DateTimeZone.UTC).getMillis
        )
      )
  }


}
