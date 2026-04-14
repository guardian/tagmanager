import React from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { hasPermission } from './util/verifyPermission';
import { withParams } from './util/withParams';

import ReactApp       from './components/ReactApp.react';
import BatchTag       from './components/BatchTag.react';
import MergeTag       from './components/MergeTag.react';
import MappingManager from './components/MappingManager.react';
import Status         from './components/Status.react';
import Audit          from './components/Audit.react';
import Unauthorised   from './components/Unauthorised.react';

import TagCreate  from './components/Tag/Create.react';
import TagDisplay from './components/Tag/Display.react';
import TagSearch  from './components/TagSearch.react';

import SectionList    from './components/SectionList/SectionList.react';
import SectionDisplay from './components/Section/Display';
import SectionCreate  from './components/Section/Create';

import PillarList    from './components/Pillar/PillarList.react';
import PillarDisplay from './components/Pillar/Display.react';
import PillarCreate  from './components/Pillar/Create.react';

import SponsorshipSearch  from './components/SponsorshipSearch.react';
import SponsorshipDisplay from './components/Sponsorship/Display';
import SponsorshipCreate  from './components/Sponsorship/Create';
import SpreadsheetBuilder from './components/Spreadsheet/SpreadsheetBuilder.react';

// Wrap class components that need URL params via this.props.routeParams
const TagDisplayWithParams         = withParams(TagDisplay);
const SectionDisplayWithParams     = withParams(SectionDisplay);
const PillarDisplayWithParams      = withParams(PillarDisplay);
const SponsorshipDisplayWithParams = withParams(SponsorshipDisplay);

function ProtectedRoute({ permission, children }) {
  if (!hasPermission(permission)) {
    return <Navigate to="/unauthorised" replace />;
  }
  return children;
}

export const router = createBrowserRouter([
  {
    path: '/',
    element: <ReactApp />,
    children: [
      { index: true, element: <TagSearch /> },
      { path: 'tag/create', element: <ProtectedRoute permission="tag_edit"><TagCreate /></ProtectedRoute> },
      { path: 'tag/:tagId', element: <TagDisplayWithParams /> },
      { path: 'batch',      element: <BatchTag /> },
      { path: 'mapping',    element: <MappingManager /> },
      { path: 'merge',      element: <ProtectedRoute permission="tag_admin"><MergeTag /></ProtectedRoute> },
      { path: 'audit',      element: <Audit /> },
      { path: 'status',     element: <Status /> },
      { path: 'section',            element: <ProtectedRoute permission="tag_admin"><SectionList /></ProtectedRoute> },
      { path: 'section/create',     element: <ProtectedRoute permission="tag_admin"><SectionCreate /></ProtectedRoute> },
      { path: 'section/:sectionId', element: <ProtectedRoute permission="tag_admin"><SectionDisplayWithParams /></ProtectedRoute> },
      { path: 'sponsorship',                element: <ProtectedRoute permission="commercial_tags"><SponsorshipSearch /></ProtectedRoute> },
      { path: 'sponsorship/create',         element: <ProtectedRoute permission="commercial_tags"><SponsorshipCreate /></ProtectedRoute> },
      { path: 'sponsorship/:sponsorshipId', element: <ProtectedRoute permission="commercial_tags"><SponsorshipDisplayWithParams /></ProtectedRoute> },
      { path: 'pillar',           element: <ProtectedRoute permission="tag_admin"><PillarList /></ProtectedRoute> },
      { path: 'pillar/create',    element: <ProtectedRoute permission="tag_admin"><PillarCreate /></ProtectedRoute> },
      { path: 'pillar/:pillarId', element: <ProtectedRoute permission="tag_admin"><PillarDisplayWithParams /></ProtectedRoute> },
      { path: 'microsite',            element: <SectionList isMicrositeView={true} /> },
      { path: 'microsite/create',     element: <SectionCreate isMicrositeView={true} /> },
      { path: 'microsite/:sectionId', element: <SectionDisplayWithParams isMicrositeView={true} /> },
      { path: 'spreadsheets', element: <SpreadsheetBuilder /> },
      { path: 'unauthorised',  element: <Unauthorised /> },
    ]
  }
]);
