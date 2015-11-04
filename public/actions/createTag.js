import tagManagerApi from '../util/tagManagerApi';

export const TAG_CREATE_REQUEST = 'TAG_CREATE_REQUEST';
export const TAG_CREATE_RECEIVE = 'TAG_CREATE_RECEIVE';
export const TAG_CREATE_ERROR = 'TAG_CREATE_ERROR';

function requestTagCreate() {
    return {
        type:       TAG_CREATE_REQUEST,
        receivedAt: Date.now()
    };
}

function recieveTagCreate(tag) {
    return {
        type:       TAG_CREATE_RECEIVE,
        tag:        tag,
        receivedAt: Date.now()
    };
}

function errorTagCreate(error) {
    return {
        type:       TAG_CREATE_ERROR,
        message:    'Could not create tag',
        error:      error,
        receivedAt: Date.now()
    };
}

export function createTag(tag) {
    return dispatch => {
        dispatch(requestTagCreate());
        return tagManagerApi.createTag(tag)
            .then(res => dispatch(recieveTagCreate(res)))
            .fail(error => dispatch(errorTagCreate(error)));
    };
}
