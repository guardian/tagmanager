import tagManagerApi from '../../util/tagManagerApi';

export const SECTION_GET_REQUEST = 'SECTION_GET_REQUEST';
export const SECTION_GET_RECEIVE = 'SECTION_GET_RECEIVE';
export const SECTION_GET_ERROR = 'SECTION_GET_ERROR';

function requestSectionGet() {
    return {
        type:       SECTION_GET_REQUEST,
        receivedAt: Date.now()
    };
}

function receiveSectionGet(section) {
    return {
        type:       SECTION_GET_RECEIVE,
        section:    section,
        receivedAt: Date.now()
    };
}

function errorSectionGet(error) {
    return {
        type:       SECTION_GET_ERROR,
        message:    'Could not get section',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getSection(id) {
    return dispatch => {
        dispatch(requestSectionGet());
        return tagManagerApi.getSection(id)
            .then(res => dispatch(receiveSectionGet(res)))
            .fail(error => dispatch(errorSectionGet(error)));
    };
}
