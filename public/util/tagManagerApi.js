
import Reqwest from 'reqwest';

export default {
  getTag: (id) => {
      return Reqwest({
          url: '/api/tag/' + id,
          contentType: 'application/json',
          method: 'get'
      });
  },
  saveTag: (id, tag) => {
    return Reqwest({
        url: '/api/tag/' + id,
        data: JSON.stringify(tag),
        contentType: 'application/json',
        method: 'put'
    });
  },

  getSections: () => {
    return Reqwest({
      url: '/api/sections',
      method: 'get',
      type: 'json'
    });
  }
};
