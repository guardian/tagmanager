import {TAG_GET_REQUEST, TAG_GET_RECEIVE, TAG_GET_ERROR} from '../actions/getTag';
import {TAG_UPDATE} from '../actions/updateTag';
import {TAG_SAVE} from '../actions/saveTag';

const saveState = {
  dirty: 'SAVE_STATE_DIRTY',
  clean: 'SAVE_STATE_CLEAN',
  error: 'SAVE_STATE_ERROR'
};

export default function tag(state = {
  tag: false,
  error: false,
  saveState: undefined
}, action) {
  switch (action.type) {
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
  case TAG_UPDATE:
    return Object.assign({}, state, {
      tag: action.tag,
      saveState: saveState.dirty
    });
  case TAG_SAVE:
    return Object.assign({}, state, {
      tag: action.tag,
      saveState: saveState.clean
    });
  default:
    return state;
  }
}
