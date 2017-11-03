import tagManagerApi from '../../util/tagManagerApi';

export const PILLARS_GET_REQUEST = 'PILLARS_GET_REQUEST';
export const PILLARS_GET_RECEIVE = 'PILLARS_GET_RECEIVE';
export const PILLARS_GET_ERROR = 'PILLARS_GET_ERROR';

function requestPillarsGet() {
    return {
        type:       PILLARS_GET_REQUEST,
        receivedAt: Date.now()
    };
}

function receivePillarsGet(pillars) {
    return {
        type:       PILLARS_GET_RECEIVE,
        pillars:   pillars,
        receivedAt: Date.now()
    };
}

function errorPillarsGet(error) {
    return {
        type:       PILLARS_GET_ERROR,
        message:    'Could not get pillars list',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getPillars() {
    return dispatch => {
        dispatch(requestPillarsGet());
        return tagManagerApi.getPillars()
            .then(res => dispatch(receivePillarsGet(res)))
            .fail(error => dispatch(errorPillarsGet(error)));
    };
}
