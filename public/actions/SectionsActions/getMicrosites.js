import tagManagerApi from '../../util/tagManagerApi';

export const MICROSITES_GET_REQUEST = 'MICROSITES_GET_REQUEST';
export const MICROSITES_GET_RECEIVE = 'MICROSITES_GET_RECEIVE';
export const MICROSITES_GET_ERROR = 'MICROSITES_GET_ERROR';

function requestMicrositesGet() {
    return {
        type:       MICROSITES_GET_REQUEST,
        receivedAt: Date.now()
    };
}

function receiveMicrositesGet(microsites) {
    return {
        type:       MICROSITES_GET_RECEIVE,
        sections:   microsites,
        receivedAt: Date.now()
    };
}

function errorMicrositesGet(error) {
    return {
        type:       MICROSITES_GET_ERROR,
        message:    'Could not get microsites list',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getMicrosites() {
    return dispatch => {
        dispatch(requestMicrositesGet());
        return tagManagerApi.getMicrosites()
            .then(res => dispatch(receiveMicrositesGet(res)))
            .fail(error => dispatch(errorMicrositesGet(error)));
    };
}
