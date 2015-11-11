import * as tagTypes from '../constants/tagTypes';

function validateMandatoryFields(mandatoryFields, tag) {

  const mandatoryFieldErrors = [];

  mandatoryFields.forEach((fieldName) => {
    if (!tag[fieldName]) {
      mandatoryFieldErrors.push({
        fieldName: fieldName,
        message: 'Mandatory field \'' + fieldName + '\' is empty.'
      });
    }
  });

  return mandatoryFieldErrors;
}

function validateBooleanFields(booleanFields, tag) {

  const booleanFieldErrors = [];

  booleanFields.forEach((fieldName) => {
    if (tag[fieldName] !== true && tag[fieldName] !== false) {
      booleanFieldErrors.push({
        fieldName: fieldName,
        message: 'Boolean field \'' + fieldName + '\' is set to \'' + tag[fieldName]
      });
    }
  });

  return booleanFieldErrors;
}

export function validateTag(tag) {
  let mandatoryFields = ['internalName', 'externalName', 'comparableValue', 'slug', 'type'];
  let booleanFields = ['hidden', 'legallySensitive'];

  if (tag.type === tagTypes.topic) {
    mandatoryFields = mandatoryFields.concat(['section', 'category']);
  }

  return []
    .concat(validateMandatoryFields(mandatoryFields, tag))
    .concat(validateBooleanFields(booleanFields, tag));
}
