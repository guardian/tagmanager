import {TAG_GET_REQUEST, TAG_GET_RECEIVE, TAG_GET_ERROR} from '../actions/TagActions/getTag';
import {TAG_UPDATE} from '../actions/TagActions/updateTag';
import {TAG_CREATE_ERROR} from '../actions/TagActions/createTag';
import {TAG_SAVE_REQUEST, TAG_SAVE_RECEIVE, TAG_SAVE_ERROR} from '../actions/TagActions/saveTag';
import {TAG_DELETE_REQUEST, TAG_DELETE_RECEIVE, TAG_DELETE_ERROR} from '../actions/TagActions/deleteTag';
import {SECTIONS_GET_REQUEST, SECTIONS_GET_RECEIVE, SECTIONS_GET_ERROR} from '../actions/SectionsActions/getSections';
import {SECTION_GET_REQUEST, SECTION_GET_RECEIVE, SECTION_GET_ERROR} from '../actions/SectionsActions/getSection';
import {SECTION_UPDATE} from '../actions/SectionsActions/updateSection';
import {SECTION_SAVE_REQUEST, SECTION_SAVE_RECEIVE, SECTION_SAVE_ERROR} from '../actions/SectionsActions/saveSection';
import {REFERENCE_TYPES_GET_REQUEST, REFERENCE_TYPES_GET_RECEIVE, REFERENCE_TYPES_GET_ERROR} from '../actions/ReferenceTypeActions/getReferenceTypes';
import {TAG_POPULATE_BLANK} from '../actions/TagActions/createTag';
import {SECTION_POPULATE_BLANK} from '../actions/SectionsActions/createSection';
import {CAPI_CLEAR_PAGES, CAPI_SWITCH_PAGE, CAPI_SEARCH_RECEIVE, CAPI_SEARCH_REQUEST, CAPI_FILTERS_UPDATE} from '../actions/CapiActions/searchCapi';
import {CLEAR_ERROR} from '../actions/UIActions/clearError';
import {SHOW_ERROR} from '../actions/UIActions/showError';

export const saveState = {
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

// UI

  case CLEAR_ERROR:
    return Object.assign({}, state, {
      error: false
    });

  case SHOW_ERROR:
    return Object.assign({}, state, {
      error: action.message
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
      tagEditable: action.tagEditable,
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
      tagEditable: action.tagEditable,
      saveState: saveState.clean
    });

// TAG CREATE

  case TAG_CREATE_ERROR:
    return Object.assign({}, state, {
      error: action.message,
      saveState: undefined
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

// TAG DELETE
  case TAG_DELETE_REQUEST:
    return Object.assign({}, state, {
      saveState: saveState.clean
    });
  case TAG_DELETE_RECEIVE:
    return Object.assign({}, state, {
      saveState: saveState.clean,
      error: action.message
    });
  case TAG_DELETE_ERROR:
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

// SECTION GET

  case SECTION_GET_REQUEST:
    return Object.assign({}, state, {
      section: false,
      saveState: undefined
    });
  case SECTION_GET_RECEIVE:
    return Object.assign({}, state, {
      section: action.section,
      saveState: saveState.clean
    });
  case SECTION_GET_ERROR:
    return Object.assign({}, state, {
      error: action.message,
      saveState: undefined
    });

// SECTION UPDATE

  case SECTION_UPDATE:
    return Object.assign({}, state, {
      section: action.section,
      saveState: saveState.dirty
    });

  case SECTION_POPULATE_BLANK:
    return Object.assign({}, state, {
      section: action.section,
      saveState: saveState.clean
    });

// SECTION SAVE

  case SECTION_SAVE_REQUEST:
    return Object.assign({}, state, {
      saveState: saveState.inprogress
    });
  case SECTION_SAVE_RECEIVE:
    return Object.assign({}, state, {
      section: action.section,
      saveState: saveState.clean
    });
  case SECTION_SAVE_ERROR:
    return Object.assign({}, state, {
      error: action.message
    });

  // REFERENCE TYPES GET

  case REFERENCE_TYPES_GET_REQUEST:
    return Object.assign({}, state, {
      referenceTypes: false
    });
  case REFERENCE_TYPES_GET_RECEIVE:
    return Object.assign({}, state, {
      referenceTypes: action.referenceTypes
    });
  case REFERENCE_TYPES_GET_ERROR:
    return Object.assign({}, state, {
      error: action.message
    });

  //CAPI SEARCH

  case CAPI_CLEAR_PAGES:
    return Object.assign({}, state, {
      capiSearch: Object.assign({}, state.capiSearch, {
          pages: {},
          count: 0,
          pageRequestCount: 0
      })
    });

  case CAPI_SWITCH_PAGE:
    return Object.assign({}, state, {
        capiSearch: Object.assign({}, state.capiSearch, {
            currentPage: action.page
        })
    });

  case CAPI_SEARCH_REQUEST:
    return Object.assign({}, state, {
      capiSearch: Object.assign({}, state.capiSearch, {
        searchTerm: action.searchTerm,
        pageRequestCount: state.capiSearch.pageRequestCount + 1
      })
    });

  case CAPI_SEARCH_RECEIVE:
    var newState = Object.assign({}, state, {
      capiSearch: Object.assign({}, state.capiSearch, {
          pages: Object.assign({}, state.capiSearch.pages),
          count: action.resultsCount,
          currentPage: action.page
        }),
    });

    var newPage = action.results;

    newState.capiSearch.pages[action.page] = newPage;
    newState.capiSearch.pageRequestCount -= 1;

    return newState;

  case CAPI_FILTERS_UPDATE:
    return Object.assign({}, state, {
      capiSearch: Object.assign({}, state.capiSearch, {
        filters: action.filters
      })
    });

  default:
    return state;
  }
}
