import tagManagerApi from '../../util/tagManagerApi';

export const TAG_SAVE_REQUEST = 'TAG_SAVE_REQUEST';
export const TAG_SAVE_RECEIVE = 'TAG_SAVE_RECEIVE';
export const TAG_SAVE_ERROR = 'TAG_SAVE_ERROR';

function requestTagSave() {
    return {
        type:       TAG_SAVE_REQUEST,
        receivedAt: Date.now()
    };
}

function recieveTagSave(tag) {
    return {
        type:       TAG_SAVE_RECEIVE,
        tag:        tag,
        receivedAt: Date.now()
    };
}

function errorTagSave(error) {
    return {
        type:       TAG_SAVE_ERROR,
        message:    'Could not save tag',
        error:      error,
        receivedAt: Date.now()
    };
}

export function saveTag(tag) {
    return dispatch => {
        dispatch(requestTagSave());
        return tagManagerApi.saveTag(tag.id, tag)
            .then(res => dispatch(recieveTagSave(res)))
            .fail(error => dispatch(errorTagSave(error)));
    };
}
