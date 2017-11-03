import tagManagerApi from '../../util/tagManagerApi';
import { browserHistory } from 'react-router'

export const PILLAR_DELETE_REQUEST = 'PILLAR_DELETE_REQUEST';
export const PILLAR_DELETE_RECEIVE = 'PILLAR_DELETE_RECEIVE';
export const PILLAR_DELETE_ERROR = 'PILLAR_DELETE_ERROR';

function requestPillarDelete() {
    return {
        type:       PILLAR_DELETE_REQUEST,
        receivedAt: Date.now()
    };
}

function recievePillarDelete(pillar) {
    browserHistory.push('/pillar');

    return {
        type:       PILLAR_DELETE_RECEIVE,
        pillar:     pillar,
        receivedAt: Date.now()
    };
}

function errorPillarDelete(error) {
    return {
        type:       PILLAR_DELETE_ERROR,
        message:    'Could not delete pillar',
        error:      error,
        receivedAt: Date.now()
    };
}

export function deletePillar(pillar) {
    return dispatch => {
        dispatch(requestPillarDelete());
        return tagManagerApi.deletePillar(pillar.id, pillar)
            .then(res => dispatch(recievePillarDelete(res)))
            .fail(error => dispatch(errorPillarDelete(error)));
    };
}
