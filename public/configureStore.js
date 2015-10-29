import { compose, createStore, applyMiddleware } from 'redux';
import thunkMiddleware from 'redux-thunk';
//import { devTools, persistState } from 'redux-devtools';
import createLogger from 'redux-logger';

const logger = createLogger({
  level: 'info',
  collapsed: true
});

import rootReducer from './reducers/rootReducer';

const createStoreWithMiddleware = compose(
  applyMiddleware(
    thunkMiddleware,
    logger
  )
  //devTools()
  // Lets you write ?debug_session=<name> in address bar to persist debug sessions
  //persistState(window.location.href.match(/[?&]debug_session=([^&]+)\b/))
)(createStore);

export default function configureStore(initialState) {
  return createStoreWithMiddleware(rootReducer, initialState);
}
