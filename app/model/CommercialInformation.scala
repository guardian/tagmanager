package model

import play.api.libs.json._
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import com.gu.tagmanagement.{CommercialInformation => ThriftCommercialInformation, IABTaxonomyInformation => ThriftIABTaxonomyInformation}

case class IABTaxonomyInformation(tagId: String, taxonomyCode: String, modelId: Option[String]) {
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

object IABTaxonomyInformation {
  implicit val iabTaxonomyFormat: OFormat[IABTaxonomyInformation] = Jsonx.formatCaseClass[IABTaxonomyInformation]

  def apply(thriftIABTaxonomyInformation: ThriftIABTaxonomyInformation): IABTaxonomyInformation =
    IABTaxonomyInformation(
      tagId = thriftIABTaxonomyInformation.tagId,
      taxonomyCode = thriftIABTaxonomyInformation.taxonomyCode, 
      modelId = thriftIABTaxonomyInformation.modelId
    )
}

case class CommercialInformation(commercialType: String, iabTaxonomyInformation: Option[IABTaxonomyInformation]) {
  def asThrift = ThriftCommercialInformation(
      commercialType = commercialType,
      iabTaxonomyInformation = iabTaxonomyInformation.map(_.asThrift)
  )

  def asExportedXml = {
    <commercialType>{this.commercialType}</commercialType>
    <iabTaxonomy>{this.iabTaxonomyInformation}</iabTaxonomy>
  }
}

object CommercialInformation {

  implicit val commercialFormat: OFormat[CommercialInformation] = Jsonx.formatCaseClass[CommercialInformation]

  def apply(thriftCommercialInformation: ThriftCommercialInformation): CommercialInformation =
    CommercialInformation(
      commercialType = thriftCommercialInformation.commercialType,
      iabTaxonomyInformation = thriftCommercialInformation.iabTaxonomyInformation.map(IABTaxonomyInformation(_))
    )
}
