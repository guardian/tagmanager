import {TAG_GET_RECEIVE} from '../actions/tagActions';

export default function(state = {}, action) {
  switch (action.type) {
  case TAG_GET_RECEIVE:
    return Object.assign({}, state, {
      tag: action.tag
    });
  default:
    return state;
  }
}
