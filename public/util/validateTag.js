import * as tagTypes from '../constants/tagTypes';

function validateMandatoryFields(mandatoryFields, object) {

  const mandatoryFieldErrors = [];

  mandatoryFields.forEach((fieldName) => {
    if (!object[fieldName]) {
      mandatoryFieldErrors.push({
        fieldName: fieldName,
        message: 'Mandatory field \'' + fieldName + '\' is empty.'
      });
    }
  });

  return mandatoryFieldErrors;
}

function validateBooleanFields(booleanFields, object) {

  const booleanFieldErrors = [];

  booleanFields.forEach((fieldName) => {
    if (object[fieldName] !== true && object[fieldName] !== false) {
      booleanFieldErrors.push({
        fieldName: fieldName,
        message: 'Boolean field \'' + fieldName + '\' is set to \'' + object[fieldName]
      });
    }
  });

  return booleanFieldErrors;
}

function validatePodcast(tag) {
  if (!tag.podcastMetadata) {
    return []; //No podcast metadata
  }

  const mandatoryPodcastFields = ['linkUrl', 'image'];

  return validateMandatoryFields(mandatoryPodcastFields, tag.podcastMetadata);
}


function validatePaidContent(tag) {
  const mandatoryPaidContentFields = ['sponsorName', 'sponsorLogo', 'sponsorLink'];

  return validateMandatoryFields(mandatoryPaidContentFields, tag.sponsorship);
}

function validateTrackingTag(tag) {

  if (!tag.trackingInformation) {
    return [{
      fieldName: 'trackingInformation',
      message: 'Mandatory tracking information is missing.'
    }];
  }

  const mandatoryTrackingFields = ['trackingType'];
  return validateMandatoryFields(mandatoryTrackingFields, tag.trackingInformation);

}

export function validateTag(tag) {
  let mandatoryFields = ['internalName', 'externalName', 'comparableValue', 'slug', 'type'];
  let booleanFields = ['hidden', 'legallySensitive'];

  let additionalErrors = []; //Use this to store other validation errors

  if (tag.type === tagTypes.topic.name) {
    mandatoryFields = mandatoryFields.concat(['section']);
  } else if (tag.type === tagTypes.series.name) {
    additionalErrors = additionalErrors.concat(validatePodcast(tag));
  } else if (tag.type === tagTypes.paidContent.name) {
    additionalErrors = additionalErrors.concat(validatePaidContent(tag));
  } else if (tag.type === tagTypes.tracking.name) {
    additionalErrors = additionalErrors.concat(validateTrackingTag(tag))
  }

  return []
    .concat(validateMandatoryFields(mandatoryFields, tag))
    .concat(validateBooleanFields(booleanFields, tag))
    .concat(additionalErrors);
}
