import React        from 'react';
import { Route, IndexRoute } from 'react-router';

import ReactApp     from '../components/ReactApp.react';
import TagCreate    from '../components/Tag/Create.react';
import TagDisplay   from '../components/Tag/Display.react';
import BatchTag     from '../components/BatchTag.react';
import MergeTag     from '../components/MergeTag.react';
import Status       from '../components/Status.react';
import TagSearch    from '../components/TagSearch.react';
import Unauthorised from '../components/Unauthorised.react';

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
        <Route name="batch" path="/batch" component={BatchTag} onEnter={requirePermission.bind(this, 'batch_tag')}/>
        <Route name="merge" path="/merge" component={MergeTag} />
        <Route name="status" path="/status" component={Status} />
        <Route name="unauthorised" path="/unauthorised" component={Unauthorised} />
        <IndexRoute component={TagSearch}/>
    </Route>
];
