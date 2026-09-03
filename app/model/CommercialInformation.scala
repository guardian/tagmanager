package model

import play.api.libs.json._
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import com.gu.tagmanagement.{CommercialInformation => ThriftCommercialInformation, IABTaxonomyInformation => ThriftIABTaxonomyInformation}
import repositories.Sequences.tagId

case class IabTaxonomyInformation(tagId: String, taxonomyCode: String, modelId: Option[String]) {
  def asThrift = ThriftIABTaxonomyInformation(
    tagId = tagId,
    taxonomyCode = taxonomyCode,
    modelId = modelId
  )

  def asExportedXml = {
    <tagId>{this.tagId}</tagId>
    <taxonomyCode>{this.taxonomyCode}</taxonomyCode>
    <modelId>{this.modelId}</modelId>
  }
}

object IabTaxonomyInformation {
  implicit val format: OFormat[IabTaxonomyInformation] = Jsonx.formatCaseClass[IabTaxonomyInformation]

  def apply(thriftIABTaxonomyInformation: ThriftIABTaxonomyInformation): IabTaxonomyInformation =
    IabTaxonomyInformation(
      tagId=thriftIABTaxonomyInformation.tagId, 
      taxonomyCode = thriftIABTaxonomyInformation.taxonomyCode, 
      modelId = thriftIABTaxonomyInformation.modelId
      )
}

case class CommercialInformation(commercialType: String, iabTaxonomyInformation: Option[IabTaxonomyInformation]) {

  def asThrift = ThriftCommercialInformation(
      commercialType = commercialType,
      // iabTaxonomyInformation = iabTaxonomyInformation.map(t=>t.asThrift)

  )

  def asExportedXml = {
    <commercialType>{this.commercialType}</commercialType>
  }
}

object CommercialInformation {

  implicit val trackingFormat: OFormat[CommercialInformation] = Jsonx.formatCaseClass[CommercialInformation]

  def apply(thriftCommercialInformation: ThriftCommercialInformation): CommercialInformation =
    CommercialInformation(
      commercialType = thriftCommercialInformation.commercialType,
      iabTaxonomyInformation = thriftCommercialInformation.iabTaxonomyInformation
    )
}
