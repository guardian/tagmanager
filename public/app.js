import React from 'react';
import { render } from 'react-dom';
import { Provider } from 'react-redux';
import Router from 'react-router';
import createBrowserHistory from 'history/lib/createBrowserHistory';

import routes from './routes/routes';
import configureStore from './util/configureStore';

const history = createBrowserHistory();
const store = configureStore();

render(
    <Provider store={store}>
      <Router routes={routes} history={history}/>
    </Provider>
, document.getElementById('react-mount'));

window.store = store;
