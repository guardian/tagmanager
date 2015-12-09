import React        from 'react';
import { Route, IndexRoute } from 'react-router';

import ReactApp     from '../components/ReactApp.react';
import TagCreate    from '../components/Tag/Create.react';
import TagDisplay   from '../components/Tag/Display.react';
import BatchTag     from '../components/BatchTag.react';
import MergeTag     from '../components/MergeTag.react';
import Status       from '../components/Status.react';
import Dashboard    from '../components/Dashboard.react';

export default [
    <Route path="/" component={ReactApp}>
        <Route name="tag" path="/tag/create" component={TagCreate} />
        <Route name="tagCreate" path="/tag/:tagId" component={TagDisplay} />
        <Route name="batch" path="/batch" component={BatchTag} />
        <Route name="merge" path="/merge" component={MergeTag} />
        <Route name="status" path="/status" component={Status} />
        <IndexRoute component={Dashboard}/>
    </Route>
];
