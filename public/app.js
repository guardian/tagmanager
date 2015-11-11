import React from 'react';
import { render } from 'react-dom';
import { Provider } from 'react-redux';
import Router from 'react-router';

import routes from './routes/routes';
import configureStore from './util/configureStore';
import history from './routes/history';

import './style/main.scss';

const store = configureStore();

// Extract config from page

const config = extractConfigFromPage();

store.dispatch({
    type:       'CONFIG_RECEIVED',
    config:     config,
    receivedAt: Date.now()
});

render(
    <Provider store={store}>
      <Router routes={routes} history={history}/>
    </Provider>
, document.getElementById('react-mount'));

function extractConfigFromPage() {

  const configEl = document.getElementById('config');

  if (!configEl) {
    return {};
  }

  return JSON.parse(configEl.innerHTML);
}
