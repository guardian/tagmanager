import tagManagerApi from '../../util/tagManagerApi';

export const CLASHING_SPONSORSHIPS_GET_REQUEST = 'CLASHING_SPONSORSHIPS_GET_REQUEST';
export const CLASHING_SPONSORSHIPS_GET_RECEIVE = 'CLASHING_SPONSORSHIPS_GET_RECEIVE';
export const CLASHING_SPONSORSHIPS_GET_ERROR = 'CLASHING_SPONSORSHIPS_GET_ERROR';

function requestClashingSponsorshipsGet() {
    return {
        type:       CLASHING_SPONSORSHIPS_GET_REQUEST,
        receivedAt: Date.now()
    };
}

function receiveClashingSponsorshipsGet(clashingSponsorships) {
    return {
        type:       CLASHING_SPONSORSHIPS_GET_RECEIVE,
        clashingSponsorships:   clashingSponsorships,
        receivedAt: Date.now()
    };
}

function errorClashingSponsorshipsGet(error) {
    return {
        type:       CLASHING_SPONSORSHIPS_GET_ERROR,
        message:    'Could not get clashing sponsorships',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getClashingSponsorships(sponsorship) {
    return dispatch => {
        dispatch(requestClashingSponsorshipsGet());
        return tagManagerApi.getClashingSponsorships(sponsorship)
            .then(res => dispatch(receiveClashingSponsorshipsGet(res)))
            .fail(error => dispatch(errorClashingSponsorshipsGet(error)));
    };
}
