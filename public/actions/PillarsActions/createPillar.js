import tagManagerApi from '../../util/tagManagerApi';
import { browserHistory } from 'react-router'


export const PILLAR_CREATE_REQUEST = 'PILLAR_CREATE_REQUEST';
export const PILLAR_CREATE_RECEIVE = 'PILLAR_CREATE_RECEIVE';
export const PILLAR_CREATE_ERROR = 'PILLAR_CREATE_ERROR';
export const PILLAR_POPULATE_BLANK = 'PILLAR_POPULATE_BLANK';

function requestPillarCreate() {
    return {
        type:       PILLAR_CREATE_REQUEST,
        receivedAt: Date.now()
    };
}

function receivePillarCreate(pillar) {
    browserHistory.push('/pillar/' + pillar.id);

    return {
        type:       PILLAR_CREATE_RECEIVE,
        pillar:     pillar,
        receivedAt: Date.now()
    };
}

function errorPillarCreate(error) {
    return {
        type:       PILLAR_CREATE_ERROR,
        message:    'Could not create pillar',
        error:      error,
        receivedAt: Date.now()
    };
}

export function createPillar(pillar) {
    return dispatch => {
        dispatch(requestPillarCreate());
        return tagManagerApi.createPillar(pillar)
            .then(res => dispatch(receivePillarCreate(res)))
            .fail(error => dispatch(errorPillarCreate(error)));
    };
}

export function populateEmptyPillar() {
    return {
        type:       PILLAR_POPULATE_BLANK,
        pillar:     {},
        receivedAt: Date.now()
    };
}
