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

// Ensure quoted strings are not split up into separate search tokens. For example:
//   buildSearch('') === '';
//   buildSearch('abc') === 'abc';
//   buildSearch('abc def xyz') === 'abc AND def AND xyz';
//   buildSearch('abc   def') === 'abc AND def';
//   buildSearch('"abc def" xyz') === '"abc def" AND xyz';
//   buildSearch('"abc def" xyz "beep   beep" bloop') === '"abc def" AND xyz AND "beep   beep" AND bloop';

function buildSearch(searchString) {
    const tokens = [];

    let token = '';
    let withinQuote = false;

    for(const char of searchString) {
        if(char === '"') {
            withinQuote = !withinQuote;
        }

        if(withinQuote) {
            token += char;
        } else {
            if(char === ' ') {
                if(token != '') {
                    tokens.push(token);
                    token = '';
                }
            } else {
                token += char;
            }
        }
    }

    // grab the last one
    if(token != '') {
        tokens.push(token);
    }

    return tokens.join(' AND ');
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
      url: getCapiUrl() + '&q=' + buildSearch(searchQueryString) + '&' + query,
      contentType: 'application/json',
      crossOrigin: true,
      method: 'get'
    });
}

export function searchPreviewContent (searchString, params) {
    const query = paramsObjectToQuery(params);

    const searchQueryString = searchString || '';

    return Reqwest({
      url: getCapiPreviewUrl() + '&q=' + buildSearch(searchQueryString) + '&' + query + '&show-fields=isLive,internalComposerCode&order-by=newest',
      contentType: 'application/json',
      crossOrigin: true,
      method: 'get'
    });
}
