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
  def logShippingStreamName: Option[String] = None
  def pandaDomain: String
  def pandaAuthCallback: String
}

class DevConfig extends Config {
  override def tagsTableName: String = "tags-dev"
  override def pandaDomain: String = "local.dev-gutools.co.uk"
  override def pandaAuthCallback: String = "https://tagmanager.local.dev-gutools.co.uk/oauthCallback"
}

class CodeConfig extends Config {
  override def tagsTableName: String = "tags-CODE"
  override def logShippingStreamName = Some("elk-CODE-KinesisStream-M03ERGK5PVD9")
  override def pandaDomain: String = "code.dev-gutools.co.uk"
  override def pandaAuthCallback: String = "https://tagmanager.code.dev-gutools.co.uk/oauthCallback"
}

class ProdConfig extends Config {
  override def tagsTableName: String = "tags-PROD"
  override def logShippingStreamName = Some("elk-PROD-KinesisStream-1PYU4KS1UEQA")
  override def pandaDomain: String = "gutools.co.uk"
  override def pandaAuthCallback: String = "https://tagmanager.gutools.co.uk/oauthCallback"
}
