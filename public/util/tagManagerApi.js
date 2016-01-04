
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

  getSection: (id) => {
    return Reqwest({
      url: '/api/section/' + id,
      method: 'get',
      type: 'json'
    });
  },

  saveSection: (id, section) => {
    return Reqwest({
        url: '/api/section/' + id,
        data: JSON.stringify(section),
        contentType: 'application/json',
        method: 'put'
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

  searchTags: (textQuery, searchFieldName, orderByField) => {

    const query = {q: textQuery};

    if (orderByField) {
      query.orderBy = orderByField;
    }

    if (searchFieldName) {
      query.searchField = searchFieldName;
    }

    return Reqwest({
        url: '/api/tags',
        method: 'get',
        data: query,
        type: 'json'
    });
  },

  getTagsByReferenceType: (referenceType) => {
    return Reqwest({
        url: '/api/tags',
        method: 'get',
        data: {referenceType: referenceType, pageSize: 1000},
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

  getAuditForTag: (tagId) => {
    return Reqwest({
      url: '/api/audit/tag/' + tagId,
      method: 'get',
      type: 'json'
    });
  },

  getAuditForOperation: (operation) => {
    return Reqwest({
      url: '/api/audit/operation/' + operation,
      method: 'get',
      type: 'json'
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
