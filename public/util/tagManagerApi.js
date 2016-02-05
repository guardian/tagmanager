
import Reqwest from 'reqwest';
import Q from 'q';
import {reEstablishSession} from 'babel?presets[]=es2015!panda-session';
import {getStore} from './storeAccessor';

function PandaReqwest(reqwestBody) {
  return Q.Promise(function(resolve, reject) {
    Reqwest(reqwestBody)
      .then(res => {
        resolve(res)
      })
      .fail(err => {
        if (err.status == 419) {
          const store = getStore();
          var reauthUrl = store.getState().config.reauthUrl;

          reEstablishSession(reauthUrl, 5000).then(
            res => {
                Reqwest(reqwestBody).then(res => resolve(res)).fail(err => reject(err));
            },
            error => {
              throw error;
            });

        } else {
          reject(err)
        }
      });
  });
}

export default {
  getTag: (id) => {
      return PandaReqwest({
          url: '/api/tag/' + id,
          contentType: 'application/json',
          method: 'get'
      });
  },

  saveTag: (id, tag) => {
    return PandaReqwest({
        url: '/api/tag/' + id,
        data: JSON.stringify(tag),
        contentType: 'application/json',
        method: 'put'
    });
  },

  deleteTag: (id, tag) => {
    return PandaReqwest({
        url: '/api/tag/' + id,
        contentType: 'application/json',
        method: 'delete'
    });
  },

  createTag: (tag) => {
    return PandaReqwest({
        url: '/api/tag',
        data: JSON.stringify(tag),
        contentType: 'application/json',
        method: 'post'
    });
  },

  getSections: () => {
    return PandaReqwest({
      url: '/api/sections',
      method: 'get',
      type: 'json'
    });
  },

  getSection: (id) => {
    return PandaReqwest({
      url: '/api/section/' + id,
      method: 'get',
      type: 'json'
    });
  },

  saveSection: (id, section) => {
    return PandaReqwest({
        url: '/api/section/' + id,
        data: JSON.stringify(section),
        contentType: 'application/json',
        method: 'put'
    });
  },

  createSection: (section) => {
    return PandaReqwest({
        url: '/api/section',
        data: JSON.stringify(section),
        contentType: 'application/json',
        method: 'post'
    });
  },

  addEditionToSection: (sectionId, editionName) => {
    return PandaReqwest({
        url: '/api/section/' + sectionId + '/edition',
        data: JSON.stringify({editionName: editionName}),
        contentType: 'application/json',
        method: 'post'
    });
  },

  removeEditionFromSection: (sectionId, editionName) => {
    return PandaReqwest({
        url: '/api/section/' + sectionId + '/edition/' + editionName,
        contentType: 'application/json',
        method: 'delete'
    });
  },

  getSponsorship: (id) => {
    return Reqwest({
      url: '/api/sponsorship/' + id,
      method: 'get',
      type: 'json'
    });
  },

  saveSponsorship: (id, sponsorship) => {
    const command = Object.assign({}, sponsorship, {
      tag: sponsorship.tag ? sponsorship.tag.id : undefined,
      section: sponsorship.section ? sponsorship.section.id : undefined
    });

    return Reqwest({
      url: '/api/sponsorship/' + id,
      data: JSON.stringify(command),
      contentType: 'application/json',
      method: 'put'
    });
  },

  createSponsorship: (sponsorship) => {
    const command = Object.assign({}, sponsorship, {
      tag: sponsorship.tag ? sponsorship.tag.id : undefined,
      section: sponsorship.section ? sponsorship.section.id : undefined
    });

    return Reqwest({
      url: '/api/sponsorship',
      data: JSON.stringify(command),
      contentType: 'application/json',
      method: 'post'
    });
  },

  getReferenceTypes: () => {
    return PandaReqwest({
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

      return PandaReqwest({
          url: '/api/checkPathInUse',
          data: query,
          method: 'get',
          type: 'json'
      });
  },

  searchTags: (textQuery, options) => {

    const query = {q: textQuery};

    if (options.orderByField) {
      query.orderBy = options.orderByField;
    }

    if (options.searchFieldName) {
      query.searchField = options.searchFieldName;
    }

    if (options.tagType) {
      query.types = options.tagType;
    }

    return PandaReqwest({
        url: '/api/tags',
        method: 'get',
        data: query,
        type: 'json'
    });
  },

  getTagsByReferenceType: (referenceType) => {
    return PandaReqwest({
        url: '/api/tags',
        method: 'get',
        data: {referenceType: referenceType, pageSize: 1000},
        type: 'json'
    });
  },

  batchTag: (contentIds, tagId, operation) => {
    const batchTagCommand = {contentIds: contentIds, tagId: tagId, operation: operation};

    return PandaReqwest({
      url: '/api/batchTag',
      data: JSON.stringify(batchTagCommand),
      contentType: 'application/json',
      method: 'post'
    });
  },

  mergeTag: (oldId, newId) => {
    const mergeTagCommand = {removingTagId: oldId, replacementTagId: newId};

    return PandaReqwest({
      url: '/api/mergeTag',
      data: JSON.stringify(mergeTagCommand),
      contentType: 'application/json',
      method: 'post'
    });
  },

  getAuditForTag: (tagId) => {
    return PandaReqwest({
      url: '/api/audit/tag/' + tagId,
      method: 'get',
      type: 'json'
    });
  },

  getAuditForTagOperation: (operation) => {
    return PandaReqwest({
      url: '/api/audit/tag/operation/' + operation,
      method: 'get',
      type: 'json'
    });
  },

  getAuditForSection: (tagId) => {
    return PandaReqwest({
      url: '/api/audit/section/' + tagId,
      method: 'get',
      type: 'json'
    });
  },

  getAuditForSectionOperation: (operation) => {
    return PandaReqwest({
      url: '/api/audit/section/operation/' + operation,
      method: 'get',
      type: 'json'
    });
  },

  getJobsByTag: (tagId) => {
    return PandaReqwest({
      url: '/api/jobs',
      method: 'get',
      data: {tagId: tagId},
      type: 'json'
    });
  },

  getAllJobs: () => {
    return PandaReqwest({
      url: '/api/jobs',
      method: 'get',
      type: 'json'
    });
  },

  deleteJob: (id) => {
    return PandaReqwest({
      url: '/api/jobs/' + id,
      method: 'delete',
      type: 'json'
    });
  }
 };
