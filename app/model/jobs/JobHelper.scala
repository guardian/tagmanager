package model.jobs

import model.{AppAudit, BatchTagOperation, Tag, TagAudit}
import model.jobs.steps._
import repositories._
import scala.concurrent.ExecutionContext

/** Utilities for starting jobs */
object JobHelper {
  def buildBatchTagJob(contentIds: List[String], toAddToTop: Option[Tag], toAddToBottom: List[Tag], toRemove: List[Tag])(implicit username: Option[String], ec: ExecutionContext): Unit = {
    def getSection(tag: Tag) = tag.section.flatMap(SectionRepository.getSection)

    val steps: List[ModifyContentTags] =
      Nil ++
      toAddToTop.map(tag => ModifyContentTags(tag, getSection(tag), contentIds, BatchTagOperation.AddToTop.entryName)) ++
      toAddToBottom.map(tag => ModifyContentTags(tag, getSection(tag), contentIds, BatchTagOperation.AddToBottom.entryName)) ++
      toRemove.map(tag => ModifyContentTags(tag, getSection(tag), contentIds, BatchTagOperation.Remove.entryName))

    var title = "Batch tag: \n"
    if (toAddToTop.isDefined) {
      title += s"    Adding '${toAddToTop.get.path}' to top. \n"
    }

    if (toAddToBottom.nonEmpty) {
      title += s"    Adding ${toAddToBottom.map(t => "'" + t.path + "'").mkString(", ")} to bottom. \n"
    }

    if (toRemove.nonEmpty) {
      title += s"    Removing ${toRemove.map(t => "'" + t.path + "'").mkString(", ")}."
    }

    JobRepository.addJob(
      Job(
        id = Sequences.jobId.getNextId,
        title = title,
        createdBy = username,
        steps = steps
      )
    )

    steps.foreach { step =>
      TagAuditRepository.upsertTagAudit(TagAudit.batchTag(step.tag, step.op, contentIds.length))
    }
  }

  def beginTagReindex()(implicit username: Option[String], ec: ExecutionContext) = {
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
    AppAuditRepository.upsertAppAudit(AppAudit.reindexTags())
  }

  def beginSectionReindex()(implicit username: Option[String], ec: ExecutionContext) = {
    ReindexProgressRepository.resetSectionReindexProgress(SectionRepository.count)

    JobRepository.addJob(
      Job(
        id = Sequences.jobId.getNextId,
        title = "Section reindex",
        createdBy = username,
        steps = List(ReindexSections())
        )
      )
    AppAuditRepository.upsertAppAudit(AppAudit.reindexSections())
  }

  def beginPillarReindex()(implicit username: Option[String], ec: ExecutionContext) = {
    ReindexProgressRepository.resetPillarReindexProgress(PillarRepository.count)

    JobRepository.addJob(
      Job(
        id = Sequences.jobId.getNextId,
        title = "Pillar reindex",
        createdBy = username,
        steps = List(ReindexPillars())
      )
    )
    AppAuditRepository.upsertAppAudit(AppAudit.reindexPillars())
  }

  def beginMergeTag(from: Tag, to: Tag)(implicit username: Option[String], ec: ExecutionContext) = {
    val fromSection = from.section.flatMap( SectionRepository.getSection(_) )
    val toSection = to.section.flatMap( SectionRepository.getSection(_) )

    JobRepository.addJob(
      Job(
        id = Sequences.jobId.getNextId,
        title = s"Merging from '${from.path}' to '${to.path}'",
        createdBy = username,
        steps = List(
          MergeTagForContent(from, to, fromSection, toSection, username)
          ) ++ removeTagSteps(from),
        tagIds = List(from.id, to.id)
        )
      )
  }

  def beginTagDeletion(tag: Tag)(implicit username: Option[String], ec: ExecutionContext) = {
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
        tagIds = List(tag.id),
        rollbackEnabled = true
        )
      )
  }

  private def removeTagSteps(tag: Tag)(implicit username: Option[String]) = List(RemoveTagPath(tag), RemoveTagFromCapi(tag), RemoveTag(tag, username))
}
