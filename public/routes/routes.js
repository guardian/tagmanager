import React        from 'react';
import { Route, IndexRoute } from 'react-router';

import ReactApp     from '../components/ReactApp.react';
import Tag          from '../components/Tag/Index.react';
import BatchTag     from '../components/BatchTag.react';
import MergeTag     from '../components/MergeTag.react';
import Status       from '../components/Status.react';
import TagSearch    from '../components/TagSearch.react';

export default [
    <Route path="/" component={ReactApp}>
        <Route name="tag" path="/tag/:tagId" component={Tag} />
        <Route name="batch" path="/batch" component={BatchTag} />
        <Route name="merge" path="/merge" component={MergeTag} />
        <Route name="status" path="/status" component={Status} />
        <IndexRoute component={TagSearch}/>
    </Route>
];