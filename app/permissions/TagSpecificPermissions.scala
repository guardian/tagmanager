package permissions

import com.gu.editorial.permissions.client.{Permission, PermissionAuthorisation, PermissionDenied, PermissionGranted}
import com.gu.pandomainauth.action.UserRequest
import com.gu.tagmanagement.TagType
import play.api.mvc.{ActionFilter, AnyContent, Result, Results}

import scala.concurrent.{ExecutionContext, Future}

object TagTypePermissionMap {
  def apply(tagType: String): Option[Permission] = {
    tagType match {
      case TagType.Tone.name => Some(Permissions.TagAdmin)
      case TagType.Blog.name => Some(Permissions.TagAdmin) //Blog is to be deprecated
      case TagType.ContentType.name => Some(Permissions.TagAdmin)
      case TagType.Publication.name => Some(Permissions.TagAdmin)
      case TagType.NewspaperBook.name => Some(Permissions.TagAdmin)
      case TagType.NewspaperBookSection.name => Some(Permissions.TagAdmin)
      case TagType.Tracking.name => Some(Permissions.TagAdmin)
      case TagType.PaidContent.name => Some(Permissions.CommercialTags)
      case _ => None
    }
  }
}

trait TagSpecificPermissionActionFilter extends ActionFilter[UserRequest] {

  val testAccess: Permission => (String => Future[PermissionAuthorisation])
  val restrictedAction: String

  override def filter[A](request: UserRequest[A]): Future[Option[Result]] = {
    request.body match {
      case b: AnyContent => {
        b.asJson.map { json =>
          val tagType: String = (json \ "type").as[String]

          val permission = TagTypePermissionMap(tagType).getOrElse { return Future.successful(None) }

          testAccess(permission)(request.user.email).map {
            case PermissionGranted => None
            case PermissionDenied => Some(Results.Unauthorized)
          }(executionContext)
          }.getOrElse {
            Future.successful(Some(Results.BadRequest("Expecting Json data")))
          }
      }
            case _ => Future.successful(Some(Results.BadRequest("Expecting body content")))
    }
  }
}

case class UpdateTagSpecificPermissionsCheck()(implicit val executionContext: ExecutionContext) extends TagSpecificPermissionActionFilter {
  val testAccess: Permission => (String => Future[PermissionAuthorisation]) = Permissions.testUser
  val restrictedAction = "update tag"
}

case class CreateTagSpecificPermissionsCheck()(implicit val executionContext: ExecutionContext) extends TagSpecificPermissionActionFilter {
  val testAccess: Permission => (String => Future[PermissionAuthorisation]) = Permissions.testUser
  val restrictedAction = "create tag"
}
