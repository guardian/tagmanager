import tagManagerApi from '../../util/tagManagerApi';
import {getStore} from '../../util/storeAccessor';


export const NEWSPAPERBOOKS_GET_REQUEST = 'NEWSPAPERBOOKS_GET_REQUEST';
export const NEWSPAPERBOOKS_GET_RECEIVE = 'NEWSPAPERBOOKS_GET_RECEIVE';
export const NEWSPAPERBOOKS_GET_ERROR = 'NEWSPAPERBOOKS_GET_ERROR';

function recieveNewspaperBooksGet(tags) {
    return {
        type:        NEWSPAPERBOOKS_GET_RECEIVE,
        tags,
        receivedAt:  Date.now()
    };
}

function errorNewspaperBooksGet(error) {
    return {
        type:       NEWSPAPERBOOKS_GET_ERROR,
        message:    'Could not get tag\'s newspaperBooks',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getNewspaperBooks(ids) {
    ids = ids || []

    return dispatch => {
        return tagManagerApi.getTags(ids)
            .then(res => {
                dispatch(recieveNewspaperBooksGet(res))
            })
            .fail(error => dispatch(errorNewspaperBooksGet(error)))
    };
}
