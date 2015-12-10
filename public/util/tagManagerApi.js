
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

  getReferenceTypes: () => {
    return Reqwest({
      url: '/api/referenceTypes',
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

  searchTags: (textQuery, orderByField) => {

    const query = {q: textQuery};

    if (orderByField) {
      query.orderBy = orderByField;
    }

    return Reqwest({
        url: '/api/tags',
        method: 'get',
        data: query,
        type: 'json'
    });
  },

  batchTag: (contentIds, tagId, operation) => {
    const batchTagCommand = {contentIds: contentIds, tagId: tagId, operation: operation};

    return Reqwest({
      url: '/api/batchTag',
      data: JSON.stringify(batchTagCommand),
      contentType: 'application/json',
      method: 'post'
    });
  },

  getJobsByTag: (tagId) => {
    return Reqwest({
      url: '/api/jobs',
      method: 'get',
      data: {tagId: tagId},
      type: 'json'
    });
  },

  getAllJobs: () => {
    return Reqwest({
      url: '/api/jobs',
      method: 'get',
      type: 'json'
    });
  }
};
