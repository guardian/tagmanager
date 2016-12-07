import Reqwest from 'reqwest';

import {getStore} from '../util/storeAccessor';

function paramsObjectToQuery (params) {

  if (!params) {
    return '';
  }

  return Object.keys(params).map((paramName) => {
    return params[paramName] ? paramName + '=' + params[paramName] : false;
  }).filter(a => a).join('&');
}

function getCapiUrl() {
  const store = getStore();
  return store.getState().config.capiUrl + '/search?api-key=' + store.getState().config.capiKey;
}


function getCapiPreviewUrl() {
  const store = getStore();
  return store.getState().config.capiPreviewUrl + '/search?api-key=' + store.getState().config.capiKey;
}

export function getByTag (tag, params) {
    const query = paramsObjectToQuery(params);

    const tagPath = tag.type === 'ContentType' ? 'type/' + tag.slug : tag.path;

    return Reqwest({
      url: getCapiUrl() + '&tag=' + tagPath  + '&' + query,
      contentType: 'application/json',
      crossOrigin: true,
      method: 'get'
    });
}

export function searchContent (searchString, params) {
    const query = paramsObjectToQuery(params);

    const searchQueryString = searchString || '';

    return Reqwest({
      url: getCapiUrl() + '&q=' + searchQueryString.replace(' ', ' AND ')  + '&' + query,
      contentType: 'application/json',
      crossOrigin: true,
      method: 'get'
    });
}

export function searchPreviewContent (searchString, params) {
    const query = paramsObjectToQuery(params);

    const searchQueryString = searchString || '';

    return Reqwest({
      url: getCapiPreviewUrl() + '&q=' + searchQueryString.replace(' ', ' AND ')  + '&' + query + '&show-fields=isLive,internalComposerCode&order-by=newest',
      contentType: 'application/json',
      crossOrigin: true,
      method: 'get'
    });
}
