import React        from 'react';
import { Route, IndexRoute } from 'react-router';

import ReactApp     from '../components/ReactApp.react';
import TagCreate    from '../components/Tag/Create.react';
import TagDisplay   from '../components/Tag/Display.react';
import BatchTag     from '../components/BatchTag.react';
import MergeTag     from '../components/MergeTag.react';
import MappingManager from '../components/MappingManager.react';
import Status       from '../components/Status.react';
import TagSearch    from '../components/TagSearch.react';
import Audit        from '../components/Audit.react';
import Unauthorised from '../components/Unauthorised.react';

import SectionList from '../components/SectionList/SectionList.react';
import SectionEdit from '../components/SectionEdit/SectionEdit.react';

import {getStore}   from '../util/storeAccessor';

function requirePermission(permissionName, nextState, replaceState) {
  const store = getStore();
  if (!store.getState().config.permissions[permissionName]) {
    replaceState(null, '/unauthorised');
  }
}

export default [
    <Route path="/" component={ReactApp}>
      <Route name="tag" path="/tag/create" component={TagCreate} />
      <Route name="tagCreate" path="/tag/:tagId" component={TagDisplay} />
      <Route name="batch" path="/batch" component={BatchTag} onEnter={requirePermission.bind(this, 'tag_admin')}/>
      <Route name="mapping" path="/mapping" component={MappingManager} />
      <Route name="merge" path="/merge" component={MergeTag} onEnter={requirePermission.bind(this, 'tag_admin')}/>
      <Route name="audit" path="/audit" component={Audit} />
      <Route name="status" path="/status" component={Status} />
      <Route name="sectionList" path="/section" component={SectionList} />
      <Route name="sectionEdit" path="/section/:sectionId" component={SectionEdit} onEnter={requirePermission.bind(this, 'tag_super_admin')}/>
      <Route name="micrositeList" path="/microsite" component={SectionList} isMicrositeView={true}/>
      <Route name="micrositeEdit" path="/microsite/:sectionId" component={SectionEdit} onEnter={requirePermission.bind(this, 'tag_super_admin')}/>
      <Route name="unauthorised" path="/unauthorised" component={Unauthorised} />
      <IndexRoute component={TagSearch}/>
    </Route>
];
