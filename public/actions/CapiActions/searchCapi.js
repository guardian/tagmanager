import debounce from 'lodash.debounce';
import {searchContent} from '../../util/capiClient';

export const CAPI_SEARCH_REQUEST = 'CAPI_SEARCH_REQUEST';
export const CAPI_SEARCH_RECEIVE = 'CAPI_SEARCH_RECEIVE';
export const CAPI_SEARCH_ERROR = 'CAPI_SEARCH_ERROR';
export const CAPI_FILTERS_UPDATE = 'CAPI_FILTERS_UPDATE';

function requestCapiSearch(searchTerm) {
    return {
        type:               CAPI_SEARCH_REQUEST,
        searchTerm:         searchTerm,
        receivedAt:         Date.now()
    };
}

function recieveCapiSearch(res, searchTerm) {
    return {
        type:               CAPI_SEARCH_RECEIVE,
        results:            res.response.results,
        resultsCount:       res.response.total,
        searchTerm:         searchTerm,
        receivedAt:         Date.now()
    };
}

function errorCapiSearch(error) {
    return {
        type:       CAPI_SEARCH_ERROR,
        message:    'Could not search CAPI',
        error:      error,
        receivedAt: Date.now()
    };
}

function _searchFn (dispatch, searchString, params) {
    return searchContent(searchString, params)
        .then(res => dispatch(recieveCapiSearch(res, searchString)))
        .fail(error => dispatch(errorCapiSearch(error)));
}

const _debouncedSearch = debounce(_searchFn, 500);

export function searchCapi(searchString, params) {
    return dispatch => {
        dispatch(requestCapiSearch(searchString));
        return _debouncedSearch(dispatch, searchString, params);
    };
}

export function updateFilters(filters) {
  return {
      type:       CAPI_FILTERS_UPDATE,
      filters:    filters,
      receivedAt: Date.now()
  };
}
