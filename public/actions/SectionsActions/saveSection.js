import tagManagerApi from '../../util/tagManagerApi';

export const SECTION_SAVE_REQUEST = 'SECTION_SAVE_REQUEST';
export const SECTION_SAVE_RECEIVE = 'SECTION_SAVE_RECEIVE';
export const SECTION_SAVE_ERROR = 'SECTION_SAVE_ERROR';

function requestSectionSave() {
    return {
        type:       SECTION_SAVE_REQUEST,
        receivedAt: Date.now()
    };
}

function recieveSectionSave(section) {
    return {
        type:       SECTION_SAVE_RECEIVE,
        section:    section,
        receivedAt: Date.now()
    };
}

function errorSectionSave(error) {
    return {
        type:       SECTION_SAVE_ERROR,
        message:    'Could not save section',
        error:      error,
        receivedAt: Date.now()
    };
}

export function saveSection(section) {
    return dispatch => {
        dispatch(requestSectionSave());
        return tagManagerApi.saveSection(section.id, section)
            .then(res => dispatch(recieveSectionSave(res)))
            .fail(error => dispatch(errorSectionSave(error)));
    };
}
