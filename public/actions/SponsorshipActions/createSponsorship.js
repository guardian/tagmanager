import history from '../../routes/history';
import tagManagerApi from '../../util/tagManagerApi';

export const SPONSORSHIP_CREATE_REQUEST = 'SPONSORSHIP_CREATE_REQUEST';
export const SPONSORSHIP_CREATE_RECEIVE = 'SPONSORSHIP_CREATE_RECEIVE';
export const SPONSORSHIP_CREATE_ERROR = 'SPONSORSHIP_CREATE_ERROR';
export const SPONSORSHIP_POPULATE_BLANK = 'SPONSORSHIP_POPULATE_BLANK';

function requestSponsorshipCreate() {
    return {
        type:       SPONSORSHIP_CREATE_REQUEST,
        receivedAt: Date.now()
    };
}

function recieveSponsorshipCreate(sponsorship) {

    return {
        type:           SPONSORSHIP_CREATE_RECEIVE,
        sponsorship:    sponsorship,
        receivedAt:     Date.now()
    };
}

function errorSponsorshipCreate(error) {
    return {
        type:       SPONSORSHIP_CREATE_ERROR,
        message:    'Could not create sponsorship',
        error:      error,
        receivedAt: Date.now()
    };
}

export function createSponsorship(sponsorship) {
    return dispatch => {
        dispatch(requestSponsorshipCreate());
        return tagManagerApi.createSponsorship(sponsorship)
            .then(res => dispatch(recieveSponsorshipCreate(res)))
            .fail(error => dispatch(errorSponsorshipCreate(error)));
    };
}

export function populateEmptySponsorship() {
  return {
      type:         SPONSORSHIP_POPULATE_BLANK,
      sponsorship:  Object.assign({}, {}),
      receivedAt:   Date.now()
  };
}
