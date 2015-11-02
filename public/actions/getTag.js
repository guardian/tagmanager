import tagManagerApi from '../util/tagManagerApi';

export const TAG_GET_REQUEST = 'TAG_GET_REQUEST';
export const TAG_GET_RECEIVE = 'TAG_GET_RECEIVE';
export const TAG_GET_ERROR = 'TAG_GET_ERROR';

function requestTagGet() {
    return {
        type:       TAG_GET_REQUEST,
        receivedAt: Date.now()
    };
}

function recieveTagGet(tag) {
    return {
        type:       TAG_GET_RECEIVE,
        tag:        tag,
        receivedAt: Date.now()
    };
}

function errorTagGet(error) {
    return {
        type:       TAG_GET_ERROR,
        message:    'Could not get tag',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getTag(id) {
    return dispatch => {
        dispatch(requestTagGet());
        return tagManagerApi.getTag(id)
            .then(res => dispatch(recieveTagGet(res)))
            .fail(error => dispatch(errorTagGet(error)));
    };
}
