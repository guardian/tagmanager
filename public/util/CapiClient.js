import Reqwest from 'reqwest';

export default function(apiUrl, apiKey) {

  const getByTag = (tag, fromDate, toDate) => {
    const fromQuery = fromDate ? '&from-date=' + fromDate : '';
    const toQuery = toDate ? '&to-date=' + toDate : '';

    return Reqwest({
      url: apiUrl + '/search?api-key=' + apiKey + '&tag=' + tag.path + fromQuery + toQuery,
      contentType: 'application/json',
      method: 'get'
    });
  };

  return {
    getByTag: getByTag
  };
}
