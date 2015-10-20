import React from 'react';
import ReactDOM from 'react-dom';
//import { Router } from 'react-router';
//import routes from '../js/routes/routes';
//import createBrowserHistory from 'history/lib/createBrowserHistory';
//
//import { Provider } from 'react-redux';
//
//import configureStore from './configureStore';
//import routerContainer from './lib/RouterContainer';
//
//const store = configureStore(window._initialState);
//const history = createBrowserHistory();
//

import ReactApp from 'components/reactApp.react';

//React.render(
//<Provider store={store}>
//{() =>
//<Router routes={routes} history={history}/>
//}
//</Provider>,
//    document.getElementById('react-mount')
//);

ReactDOM.render(<ReactApp />, document.getElementById('react-mount'));

//routerContainer.set(history);
//
//// Make dnd work??
//
//window.addEventListener('dragover', function(e) {
//    e = e || event;
//    e.preventDefault();
//}, false);
//window.addEventListener('drop', function(e) {
//    e = e || event;
//    e.preventDefault();
//}, false);