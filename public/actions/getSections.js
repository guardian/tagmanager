import tagManagerApi from '../util/tagManagerApi';

export const SECTIONS_GET_REQUEST = 'SECTIONS_GET_REQUEST';
export const SECTIONS_GET_RECEIVE = 'SECTIONS_GET_RECEIVE';
export const SECTIONS_GET_ERROR = 'SECTIONS_GET_ERROR';

function requestSectionsGet() {
    return {
        type:       SECTIONS_GET_REQUEST,
        receivedAt: Date.now()
    };
}

function receiveSectionsGet(sections) {
    return {
        type:       SECTIONS_GET_RECEIVE,
        sections:   sections,
        receivedAt: Date.now()
    };
}

function errorSectionsGet(error) {
    return {
        type:       SECTIONS_GET_ERROR,
        message:    'Could not get sections list',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getSections() {
    return dispatch => {
        dispatch(requestSectionsGet());
        return tagManagerApi.getSections()
            .then(res => dispatch(receiveSectionsGet(res)))
            .fail(error => dispatch(errorSectionsGet(error)));
    };
}
