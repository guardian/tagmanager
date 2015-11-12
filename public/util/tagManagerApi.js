
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

  createTag: (tag) => {
    return Reqwest({
        url: '/api/tag',
        data: JSON.stringify(tag),
        contentType: 'application/json',
        method: 'post'
    });
  },

  getSections: () => {
    return Reqwest({
      url: '/api/sections',
      method: 'get',
      type: 'json'
    });
  },

  checkPathInUse: (type, slug, section) => {
      const query = {type: type, slug: slug};

      if (section) {
          query.section = section;
      }

      return Reqwest({
          url: '/api/checkPathInUse',
          data: query,
          method: 'get',
          type: 'json'
      });
  },

  searchTags: (query, orderByField) => {

    const queries = [{
      name: 'q',
      value: query
    }];

    if (orderByField) {
      queries.push({
        name: 'orderBy',
        value: orderByField
      });
    }

    return Reqwest({
        url: '/api/tags',
        method: 'get',
        data: queries,
        type: 'json'
    });
  }
};
