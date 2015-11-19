import Reqwest from 'reqwest';

function paramsObjectToQuery (params) {

  if (!params) {
    return '';
  }

  return Object.keys(params).map((paramName) => {
    return paramName + '=' + params[paramName];
  }).join('&');
}

export default function(apiUrl, apiKey) {

  const getByTag = (tag, params) => {
    const query = paramsObjectToQuery(params);

    return Reqwest({
      url: apiUrl + '/search?api-key=' + apiKey + '&tag=' + tag.path  + '&' + query,
      contentType: 'application/json',
      method: 'get'
    });
  };

  const searchContent = (searchString, params) => {
    const query = paramsObjectToQuery(params);

    return Reqwest({
      url: apiUrl + '/search?api-key=' + apiKey + '&q=' + searchString  + '&' + query,
      contentType: 'application/json',
      method: 'get'
    });
  };

  return {
    getByTag: getByTag,
    searchContent: searchContent
  };
}
