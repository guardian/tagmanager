import tagManagerApi from '../../util/tagManagerApi';

export const PILLAR_GET_REQUEST = 'PILLAR_GET_REQUEST';
export const PILLAR_GET_RECEIVE = 'PILLAR_GET_RECEIVE';
export const PILLAR_GET_ERROR = 'PILLAR_GET_ERROR';

function requestPillarGet() {
    return {
        type:       PILLAR_GET_REQUEST,
        receivedAt: Date.now()
    };
}

function receivePillarGet(pillar) {
    return {
        type:       PILLAR_GET_RECEIVE,
        pillar:    pillar,
        receivedAt: Date.now()
    };
}

function errorPillarGet(error) {
    return {
        type:       PILLAR_GET_ERROR,
        message:    'Could not get pillar',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getPillar(id) {
    return dispatch => {
        dispatch(requestPillarGet());
        return tagManagerApi.getPillar(id)
            .then(res => dispatch(receivePillarGet(res)))
            .fail(error => dispatch(errorPillarGet(error)));
    };
}
