
import Reqwest from 'reqwest';

export default {
  get: (id) => {
      return Reqwest({
          url: '/api/tag/' + id,
          contentType: 'application/json',
          method: 'get'
      });
  }
};
