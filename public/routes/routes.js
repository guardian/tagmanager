import React        from 'react';
import { Route, IndexRoute } from 'react-router';

import ReactApp     from '../components/ReactApp.react';
import BatchTag     from '../components/BatchTag.react';
import MergeTag     from '../components/MergeTag.react';
import MappingManager from '../components/MappingManager.react';
import Status       from '../components/Status.react';
import Audit        from '../components/Audit.react';
import Unauthorised from '../components/Unauthorised.react';

import TagCreate    from '../components/Tag/Create.react';
import TagDisplay   from '../components/Tag/Display.react';
import TagSearch    from '../components/TagSearch.react';

import SectionList from '../components/SectionList/SectionList.react';
import SectionDisplay from '../components/Section/Display';
import SectionCreate from '../components/Section/Create';

import SponsorshipSearch from '../components/SponsorshipSearch.react';
import SponsorshipDisplay from '../components/Sponsorship/Display';
import SponsorshipCreate from '../components/Sponsorship/Create';

import {getStore}   from '../util/storeAccessor';
import {clearError} from '../actions/UIActions/clearError';
import {hasPermission} from '../util/verifyPermission';

function requirePermission(permissionName, nextState, replaceState) {
  if (!hasPermission(permissionName)) {
    replaceState(null, '/unauthorised');
  }
}

function clearErrorBar() {
    const store = getStore();
    store.dispatch(clearError());
}

export default [
    <Route path="/" component={ReactApp}>
      <Route name="tag" path="/tag/create" component={TagCreate} />
      <Route name="tagCreate" path="/tag/:tagId" component={TagDisplay} onLeave={clearErrorBar.bind(this)} />
      <Route name="batch" path="/batch" component={BatchTag}/>
      <Route name="mapping" path="/mapping" component={MappingManager} />
      <Route name="merge" path="/merge" component={MergeTag} onEnter={requirePermission.bind(this, 'tag_admin')}/>
      <Route name="audit" path="/audit" component={Audit} />
      <Route name="status" path="/status" component={Status} />
      <Route name="sectionList" path="/section" component={SectionList} onEnter={requirePermission.bind(this, 'tag_admin')} />
      <Route name="sectionCreate" path="/section/create" component={SectionCreate} onEnter={requirePermission.bind(this, 'tag_admin')} />
      <Route name="sectionEdit" path="/section/:sectionId" component={SectionDisplay} onEnter={requirePermission.bind(this, 'tag_admin')}/>
      <Route name="sponsorshipSearch" path="/sponsorship" component={SponsorshipSearch} onEnter={requirePermission.bind(this, 'commercial_tags')} />
      <Route name="sponsorshipCreate" path="/sponsorship/create" component={SponsorshipCreate} onEnter={requirePermission.bind(this, 'commercial_tags')} />
      <Route name="sponsorshipEdit" path="/sponsorship/:sponsorshipId" component={SponsorshipDisplay} onEnter={requirePermission.bind(this, 'commercial_tags')}/>
      <Route name="micrositeList" path="/microsite" component={SectionList} isMicrositeView={true}/>
      <Route name="micrositeCreate" path="/microsite/create" component={SectionCreate} isMicrositeView={true}/>
      <Route name="micrositeEdit" path="/microsite/:sectionId" component={SectionDisplay} isMicrositeView={true} />
      <Route name="unauthorised" path="/unauthorised" component={Unauthorised} />
      <IndexRoute component={TagSearch}/>
    </Route>
];
