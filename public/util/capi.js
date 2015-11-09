import Reqwest from 'reqwest';

const CAPI_BASE = 'https://internal.content.guardianapis.com';

export default {
  getByTag: (tag, fromDate, toDate) => {
    const fromQuery = fromDate ? '&from-date=' + fromDate : '';
    const toQuery = toDate ? '&to-date=' + toDate : '';

    return Reqwest({
      url: CAPI_BASE + '/search?tag=' + tag.path + fromQuery + toQuery,
      contentType: 'application/json',
      method: 'get'
    });
  }
};
