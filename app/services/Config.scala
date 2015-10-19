package services

object Config extends AwsInstanceTags {

  lazy val conf = readTag("Stage") match {
    case Some("PROD") =>    new ProdConfig
    case Some("CODE") =>    new CodeConfig
    case _ =>               new DevConfig
  }

  def apply() = {
    conf
  }
}

sealed trait Config {
  def tagsTableName: String
}

class DevConfig extends Config {
  override def tagsTableName: String = "tags-dev"
}

class CodeConfig extends Config {
  override def tagsTableName: String = "tags-CODE"
}

class ProdConfig extends Config {
  override def tagsTableName: String = "tags-PROD"
}
