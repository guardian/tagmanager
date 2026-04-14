/**
 * Programmatic navigation utility for use outside React components
 * (e.g. Redux action creators). The navigate function is set from
 * app.js after the router is created.
 */
let _navigate = null;

export function setNavigate(fn) {
  _navigate = fn;
}

export const browserHistory = {
  push: (path) => {
    if (_navigate) {
      _navigate(path);
    } else {
      console.warn('browserHistory.push called before navigate was initialized');
    }
  }
};
