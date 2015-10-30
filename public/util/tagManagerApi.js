
import Reqwest from 'reqwest';

export default {
  get: (id) => {
      return Reqwest({
          url: '/api/tag/' + id,
          contentType: 'application/json',
          method: 'get'
      });
  },
  save: (id, tag) => {
    return Reqwest({
        url: '/api/tag/' + id,
        data: JSON.stringify(tag),
        contentType: 'application/json',
        method: 'put'
    });
  }
};
