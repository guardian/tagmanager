import tagManagerApi from '../../util/tagManagerApi';
import {getStore} from '../../util/storeAccessor';
import {hasPermission} from '../../util/verifyPermission';

export const TAG_GET_REQUEST = 'TAG_GET_REQUEST';
export const TAG_GET_RECEIVE = 'TAG_GET_RECEIVE';
export const TAG_GET_ERROR = 'TAG_GET_ERROR';

function requestTagGet() {
    return {
        type:       TAG_GET_REQUEST,
        receivedAt: Date.now()
    };
}

function recieveTagGet(tag, canEdit) {
    return {
        type:        TAG_GET_RECEIVE,
        tag:         tag,
        tagEditable: canEdit,
        receivedAt:  Date.now()
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
            .then(res => {

              const store = getStore();
              var permitted = store.getState().config.permittedTagTypes;
              var tagType = res.type;

              if (!permitted.some((e, i, a) => e == tagType) || !hasPermission('tag_edit')) {
                dispatch(recieveTagGet(res, false));
              } else {
                dispatch(recieveTagGet(res, true));
              }
            })
            .fail(error => dispatch(errorTagGet(error)));
    };
}
