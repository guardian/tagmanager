import tagManagerApi from '../../util/tagManagerApi';
import history from '../../routes/history';

export const TAG_DELETE_REQUEST = 'TAG_DELETE_REQUEST';
export const TAG_DELETE_RECEIVE = 'TAG_DELETE_RECEIVE';
export const TAG_DELETE_ERROR = 'TAG_DELETE_ERROR';

function requestTagDelete() {
    return {
        type:       TAG_DELETE_REQUEST,
        receivedAt: Date.now()
    };
}

function recieveTagDelete(tagId) {
  history.replaceState(null, '/');

    return {
        type:       TAG_DELETE_RECEIVE,
        tagId:      tagId,
        receivedAt: Date.now()
    };
}

function errorTagDelete(error) {
    return {
        type:       TAG_DELETE_ERROR,
        message:    'Could not delete tag',
        error:      error,
        receivedAt: Date.now()
    };
}

export function deleteTag(tag) {
    return dispatch => {
        dispatch(requestTagDelete());
        return tagManagerApi.deleteTag(tag.id)
            .then(res => dispatch(recieveTagDelete(tag.id)))
            .fail(error => dispatch(errorTagDelete(error)));
    };
}
