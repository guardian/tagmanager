import tagManagerApi from '../../util/tagManagerApi';

export const PILLAR_SAVE_REQUEST = 'PILLAR_SAVE_REQUEST';
export const PILLAR_SAVE_RECEIVE = 'PILLAR_SAVE_RECEIVE';
export const PILLAR_SAVE_ERROR = 'PILLAR_SAVE_ERROR';

function requestPillarSave() {
    return {
        type:       PILLAR_SAVE_REQUEST,
        receivedAt: Date.now()
    };
}

function recievePillarSave(pillar) {
    return {
        type:       PILLAR_SAVE_RECEIVE,
        pillar:    pillar,
        receivedAt: Date.now()
    };
}

function errorPillarSave(error) {
    return {
        type:       PILLAR_SAVE_ERROR,
        message:    'Could not save pillar',
        error:      error,
        receivedAt: Date.now()
    };
}

export function savePillar(pillar) {
    return dispatch => {
        dispatch(requestPillarSave());
        return tagManagerApi.savePillar(pillar.id, pillar)
            .then(res => dispatch(recievePillarSave(res)))
            .fail(error => dispatch(errorPillarSave(error)));
    };
}
