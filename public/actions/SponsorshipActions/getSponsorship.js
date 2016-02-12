import tagManagerApi from '../../util/tagManagerApi';

export const SPONSORSHIP_GET_REQUEST = 'SPONSORSHIP_GET_REQUEST';
export const SPONSORSHIP_GET_RECEIVE = 'SPONSORSHIP_GET_RECEIVE';
export const SPONSORSHIP_GET_ERROR = 'SPONSORSHIP_GET_ERROR';

function requestSponsorshipGet() {
    return {
        type:       SPONSORSHIP_GET_REQUEST,
        receivedAt: Date.now()
    };
}

function receiveSponsorshipGet(sponsorship) {
    return {
        type:         SPONSORSHIP_GET_RECEIVE,
        sponsorship:  sponsorship,
        receivedAt:   Date.now()
    };
}

function errorSponsorshipGet(error) {
    return {
        type:       SPONSORSHIP_GET_ERROR,
        message:    'Could not get sponsorship',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getSponsorship(id) {
    return dispatch => {
        dispatch(requestSponsorshipGet());
        return tagManagerApi.getSponsorship(id)
            .then(res => dispatch(receiveSponsorshipGet(res)))
            .fail(error => dispatch(errorSponsorshipGet(error)));
    };
}
