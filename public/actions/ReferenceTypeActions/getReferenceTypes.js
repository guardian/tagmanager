import tagManagerApi from '../../util/tagManagerApi';

export const REFERENCE_TYPES_GET_REQUEST = 'REFERENCE_TYPES_GET_REQUEST';
export const REFERENCE_TYPES_GET_RECEIVE = 'REFERENCE_TYPES_GET_RECEIVE';
export const REFERENCE_TYPES_GET_ERROR = 'REFERENCE_TYPES_GET_ERROR';

function requestReferenceTypesGet() {
    return {
        type:       REFERENCE_TYPES_GET_REQUEST,
        receivedAt: Date.now()
    };
}

function receiveReferenceTypesGet(referenceTypes) {
    return {
        type:             REFERENCE_TYPES_GET_RECEIVE,
        referenceTypes:   referenceTypes,
        receivedAt:       Date.now()
    };
}

function errorReferenceTypesGet(error) {
    return {
        type:       REFERENCE_TYPES_GET_ERROR,
        message:    'Could not get reference types',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getReferenceTypes() {
    return dispatch => {
        dispatch(requestReferenceTypesGet());
        return tagManagerApi.getReferenceTypes()
            .then(res => dispatch(receiveReferenceTypesGet(res)))
            .fail(error => dispatch(errorReferenceTypesGet(error)));
    };
}
