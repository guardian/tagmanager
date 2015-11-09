import React from 'react';
import { render } from 'react-dom';
import { Provider } from 'react-redux';
import Router from 'react-router';

import routes from './routes/routes';
import configureStore from './util/configureStore';
import history from './routes/history';

import './style/main.scss';

const store = configureStore();

render(
    <Provider store={store}>
      <Router routes={routes} history={history}/>
    </Provider>
, document.getElementById('react-mount'));

window.store = store;
