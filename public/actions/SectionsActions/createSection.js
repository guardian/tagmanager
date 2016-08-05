import tagManagerApi from '../../util/tagManagerApi';
import { browserHistory } from 'react-router'


export const SECTION_CREATE_REQUEST = 'SECTION_CREATE_REQUEST';
export const SECTION_CREATE_RECEIVE = 'SECTION_CREATE_RECEIVE';
export const SECTION_CREATE_ERROR = 'SECTION_CREATE_ERROR';
export const SECTION_POPULATE_BLANK = 'SECTION_POPULATE_BLANK';

function requestSectionCreate() {
    return {
        type:       SECTION_CREATE_REQUEST,
        receivedAt: Date.now()
    };
}

function recieveSectionCreate(section) {
    const redirectPath = section.isMicrosite ? '/microsite/' + section.id : '/section/' + section.id;
    browserHistory.push(redirectPath);

    return {
        type:       SECTION_CREATE_RECEIVE,
        section:    section,
        receivedAt: Date.now()
    };
}

function errorSectionCreate(error) {
    return {
        type:       SECTION_CREATE_ERROR,
        message:    'Could not create section',
        error:      error,
        receivedAt: Date.now()
    };
}

export function createSection(section) {
    return dispatch => {
        dispatch(requestSectionCreate());
        return tagManagerApi.createSection(section)
            .then(res => dispatch(recieveSectionCreate(res)))
            .fail(error => dispatch(errorSectionCreate(error)));
    };
}

export function populateEmptySection(isMicrosite) {
  return {
      type:       SECTION_POPULATE_BLANK,
      section:    Object.assign({}, {
                    isMicrosite: !!isMicrosite
                  }),
      receivedAt: Date.now()
  };
}
