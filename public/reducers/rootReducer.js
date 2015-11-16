import {TAG_GET_REQUEST, TAG_GET_RECEIVE, TAG_GET_ERROR} from '../actions/getTag';
import {TAG_UPDATE} from '../actions/updateTag';
import {TAG_SAVE_REQUEST, TAG_SAVE_RECEIVE, TAG_SAVE_ERROR} from '../actions/saveTag';
import {SECTIONS_GET_REQUEST, SECTIONS_GET_RECEIVE, SECTIONS_GET_ERROR} from '../actions/getSections';
import {TAG_POPULATE_BLANK} from '../actions/createTag';
import {CAPI_SEARCH_RECEIVE, CAPI_SEARCH_REQUEST} from '../actions/searchCapi';

const saveState = {
  dirty: 'SAVE_STATE_DIRTY',
  clean: 'SAVE_STATE_CLEAN',
  inprogress: 'SAVE_STATE_INPROGRESS',
  error: 'SAVE_STATE_ERROR'
};

export default function tag(state = {
  tag: false,
  error: false,
  saveState: undefined,
  config: {},
  capiSearch: {}
}, action) {
  switch (action.type) {

// CONFIG

  case 'CONFIG_RECEIVED':
    return Object.assign({}, state, {
      config: action.config
    });

// TAG GET

  case TAG_GET_REQUEST:
    return Object.assign({}, state, {
      tag: false,
      saveState: undefined
    });
  case TAG_GET_RECEIVE:
    return Object.assign({}, state, {
      tag: action.tag,
      saveState: saveState.clean
    });
  case TAG_GET_ERROR:
    return Object.assign({}, state, {
      error: action.message,
      saveState: undefined
    });

// TAG UPDATE

  case TAG_UPDATE:
    return Object.assign({}, state, {
      tag: action.tag,
      saveState: saveState.dirty
    });

  case TAG_POPULATE_BLANK:
    return Object.assign({}, state, {
      tag: action.tag,
      saveState: saveState.clean
    });

// TAG SAVE

  case TAG_SAVE_REQUEST:
    return Object.assign({}, state, {
      saveState: saveState.inprogress
    });
  case TAG_SAVE_RECEIVE:
    return Object.assign({}, state, {
      tag: action.tag,
      saveState: saveState.clean
    });
  case TAG_SAVE_ERROR:
    return Object.assign({}, state, {
      error: action.message
    });

// SECTIONS GET

  case SECTIONS_GET_REQUEST:
    return Object.assign({}, state, {
      sections: false
    });
  case SECTIONS_GET_RECEIVE:
    return Object.assign({}, state, {
      sections: action.sections
    });
  case SECTIONS_GET_ERROR:
    return Object.assign({}, state, {
      error: action.message
    });

  //CAPI SEARCH

  case CAPI_SEARCH_REQUEST:
    return Object.assign({}, state, {
      capiSearch: Object.assign({}, state.capiSearch, {
        searchTerm: action.searchTerm
      })
    });

  case CAPI_SEARCH_RECEIVE:
    return Object.assign({}, state, {
      capiSearch: Object.assign({}, state.capiSearch, {
        results: action.results,
        count: action.resultsCount
      })
    });

  default:
    return state;
  }
}
