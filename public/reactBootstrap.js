import React from 'react';
import { render } from 'react-dom';
import Router from 'react-router';
import routes from './routes/routes';
import createBrowserHistory from 'history/lib/createBrowserHistory'

import ReactApp from 'components/ReactApp.react';

let history = createBrowserHistory();

render(<Router history={history} routes={routes} />, document.getElementById('react-mount'));
