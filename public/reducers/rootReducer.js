import {TAG_GET_RECEIVE, TAG_GET_ERROR} from '../actions/tagActions';

export default function tag(state = {tag: false}, action) {
  switch (action.type) {
  case TAG_GET_RECEIVE:
    return Object.assign({}, state, {
      tag: action.tag
    });
  case TAG_GET_ERROR:
    return Object.assign({}, state, {
      error: action.message
    });
  default:
    return state;
  }
}
