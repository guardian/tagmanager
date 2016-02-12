import tagManagerApi from '../../util/tagManagerApi';

export const SPONSORSHIP_SAVE_REQUEST = 'SPONSORSHIP_SAVE_REQUEST';
export const SPONSORSHIP_SAVE_RECEIVE = 'SPONSORSHIP_SAVE_RECEIVE';
export const SPONSORSHIP_SAVE_ERROR = 'SPONSORSHIP_SAVE_ERROR';

function requestSponsorshipSave() {
    return {
        type:       SPONSORSHIP_SAVE_REQUEST,
        receivedAt: Date.now()
    };
}

function receiveSponsorshipSave(sponsorship) {
    return {
        type:         SPONSORSHIP_SAVE_RECEIVE,
        sponsorship:  sponsorship,
        receivedAt:   Date.now()
    };
}

function errorSponsorshipSave(error) {
    return {
        type:       SPONSORSHIP_SAVE_ERROR,
        message:    'Could not save sponsorship',
        error:      error,
        receivedAt: Date.now()
    };
}

export function saveSponsorship(sponsorship) {
    return dispatch => {
        dispatch(requestSponsorshipSave());
        return tagManagerApi.saveSponsorship(sponsorship.id, sponsorship)
            .then(res => dispatch(receiveSponsorshipSave(res)))
            .fail(error => dispatch(errorSponsorshipSave(error)));
    };
}
