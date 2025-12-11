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

  let paidContentErrors = [];

  if (!tag.sponsorship) {
    return [{
      fieldName: 'sponsorship',
      message: 'Mandatory paid content information is missing.'
    }];
  }

  if (!tag.paidContentInformation || !tag.paidContentInformation.paidContentType) {
    paidContentErrors.push({
      fieldName: 'paidContentType',
      message: "Mandatory field 'paid content type' is empty"
    });
  }

  if (tag.paidContentInformation && tag.paidContentInformation.paidContentType === 'HostedContent') {

    //Hosted Content Validation
    const hexCodeRegex = /^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$/;

    if (!tag.paidContentInformation.campaignColour || !tag.paidContentInformation.campaignColour.match(hexCodeRegex)) {
      paidContentErrors.push({
        fieldName: 'campaignColour',
        message: 'Mandatory campaignColour information is missing or is invalid'
      });
    }
  }

  const mandatoryPaidContentFields = ['sponsorName', 'sponsorLogo', 'sponsorLink'];

  return paidContentErrors.concat(validateMandatoryFields(mandatoryPaidContentFields, tag.sponsorship));
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

function validateTagName(tag) {
  const errors = [];
  if (tag.externalName && tag.externalName.endsWith(' ')) {
    errors.push({
      fieldName: 'externalName',
      message: 'External name has a trailing space.'
    });
  }

  if (tag.externalName && tag.externalName.startsWith(' ')) {
    errors.push({
      fieldName: 'externalName',
      message: 'External name has a leading space.'
    });
  }

  return errors;
}

function validateContributorInformation(tag) {
  const mandatoryContributorFields = ['lastName'];
  return validateMandatoryFields(mandatoryContributorFields, tag.contributorInformation || { lastName: null });
}

export function validateTag(tag) {
  let mandatoryFields = ['internalName', 'externalName', 'comparableValue', 'slug', 'type'];
  let booleanFields = ['hidden', 'legallySensitive'];

  let additionalErrors = []; //Use this to store other validation errors

  if (tag.type === tagTypes.topic.name) {
    mandatoryFields = mandatoryFields.concat(['section']);
  } else if (tag.type === tagTypes.series.name) {
    mandatoryFields = mandatoryFields.concat(['section']);
    additionalErrors = additionalErrors.concat(validatePodcast(tag));
  } else if (tag.type === tagTypes.paidContent.name) {
    additionalErrors = additionalErrors.concat(validatePaidContent(tag));
  } else if (tag.type === tagTypes.tracking.name) {
    additionalErrors = additionalErrors.concat(validateTrackingTag(tag));
  } else if (tag.type === tagTypes.contributor.name) {
    additionalErrors = additionalErrors.concat(validateContributorInformation(tag));
  }

  additionalErrors = additionalErrors.concat(validateTagName(tag));

  return []
    .concat(validateMandatoryFields(mandatoryFields, tag))
    .concat(validateBooleanFields(booleanFields, tag))
    .concat(additionalErrors);
}
