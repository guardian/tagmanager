import Reqwest from 'reqwest';
import Q from 'q';
import {reEstablishSession} from 'babel-loader?presets[]=es2015!panda-session';
import {getStore} from './storeAccessor';

export function PandaReqwest(reqwestBody) {
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
      tags: sponsorship.tags ? sponsorship.tags.map(t => t.id) : undefined,
      sections: sponsorship.sections ? sponsorship.sections.map(s => s.id) : undefined
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
      tags: sponsorship.tags ? sponsorship.tags.map(t => t.id) : undefined,
      sections: sponsorship.sections ? sponsorship.sections.map(s => s.id) : undefined
    });

    return Reqwest({
      url: '/api/sponsorship',
      data: JSON.stringify(command),
      contentType: 'application/json',
      method: 'post'
    });
  },

  searchSponsorships: (options) => {
    const query = {
      q: options.searchString,
      status: options.status,
      type: options.type
    };

    if (options.sortBy) {
      query.sortBy = options.sortBy;
    }

    return PandaReqwest({
      url: '/api/sponsorships',
      method: 'get',
      data: query,
      type: 'json'
    });
  },

  getActiveSponsorhipsForTag: (tagId) => {
    return PandaReqwest({
      url: '/api/tag/' + tagId + '/activeSponsorships',
      method: 'get',
      type: 'json'
    });
  },

  getActiveSponsorhipsForSection: (sectionId) => {
    return PandaReqwest({
      url: '/api/section/' + sectionId + '/activeSponsorships',
      method: 'get',
      type: 'json'
    });
  },

  getClashingSponsorships: (sponsorship) => {
    const command = {};

    if(sponsorship.id) {command.id = sponsorship.id;}
    if(sponsorship.validFrom) {command.validFrom = sponsorship.validFrom;}
    if(sponsorship.validTo) {command.validTo = sponsorship.validTo;}
    if(sponsorship.tags) {command.tagIds = sponsorship.tags.map(t => t.id).toString();}
    if(sponsorship.sections) {command.sectionIds = sponsorship.sections.map(s => s.id).toString();}
    if(sponsorship.targeting && sponsorship.targeting.validEditions) {command.editions = sponsorship.targeting.validEditions.toString();}

    return Reqwest({
      url: '/api/clashingSponsorships',
      data: command,
      contentType: 'application/json',
      method: 'get'
    });
  },

  getReferenceTypes: () => {
    return PandaReqwest({
      url: '/api/referenceTypes',
      method: 'get',
      type: 'json'
    });
  },

  checkPathInUse: (tagType, slug, section, tagSubType) => {
      const query = {tagType: tagType, slug: slug};

      if (section) {
          query.section = section;
      }

      if (tagSubType) {
        query.tagSubType = tagSubType
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


    if (options.page) {
      query.page = options.page;
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

  batchTag: (contentIds, toAddToTop, toAddToBottom, toRemove) => {
    const batchTagCommand = {contentIds, toAddToTop, toAddToBottom, toRemove};

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
  },

  rollbackJob: (id) => {
    return PandaReqwest({
      url: '/api/jobs/rollback/' + id,
      method: 'put',
      type: 'json'
    });
  },

  unexpireContentForSection: (sectionId) => {
    return PandaReqwest({
      url: '/support/unexpireSectionContent',
      method: 'post',
      contentType: 'application/json',
      data: JSON.stringify({sectionId: sectionId})
    });
  },

  expireContentForSection: (sectionId) => {
    return PandaReqwest({
      url: '/support/expireSectionContent',
      method: 'post',
      contentType: 'application/json',
      data: JSON.stringify({sectionId: sectionId})
    });
  },

  getPillars: () => {
    return PandaReqwest({
      url: '/api/pillars',
      method: 'get',
      type: 'json'
    });
  },

  getPillar: (id) => {
    return PandaReqwest({
      url: '/api/pillar/' + id,
      method: 'get',
      type: 'json'
    });
  },

  savePillar: (id, pillar) => {
    return PandaReqwest({
      url: '/api/pillar/' + id,
      data: JSON.stringify(pillar),
      contentType: 'application/json',
      method: 'put'
    });
  },

  createPillar: (pillar) => {
    return PandaReqwest({
      url: '/api/pillar',
      data: JSON.stringify(pillar),
      contentType: 'application/json',
      method: 'post'
    });
  },

  deletePillar: (id) => {
    return PandaReqwest({
      url: '/api/pillar/' + id,
      contentType: 'application/json',
      method: 'delete'
    });
  }
};
