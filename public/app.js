import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import { RouterProvider } from 'react-router-dom';

import configureStore from './util/configureStore';
import { setStore } from './util/storeAccessor';
import { router } from './router';
import { setNavigate } from './util/navigate';

import './style/main.scss';
import '@guardian/prosemirror-editor/dist/style.css';

function extractConfigFromPage() {
  const configEl = document.getElementById('config');
  if (!configEl) {
    return {};
  }
  return JSON.parse(configEl.innerHTML);
}

const store = configureStore();
const config = extractConfigFromPage();

setStore(store);
setNavigate(router.navigate.bind(router));

store.dispatch({
  type:       'CONFIG_RECEIVED',
  config:     config,
  receivedAt: Date.now()
});

ReactDOM.render(
  <Provider store={store}>
    <RouterProvider router={router} />
  </Provider>,
  document.getElementById('react-mount')
);
