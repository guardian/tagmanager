import React from 'react';
import ProgressSpinner from '../components/utils/ProgressSpinner.react';

export const prettyJobStatus = {
  'waiting': 'In progress',
  'owned': 'In progress',
  'complete': 'Done',
  'failed': 'Failed',
  'rolledback': 'Rolled back'
};

export const prettyStepType = {
  'remove-tag-from-content': 'Remove tag from content',
  'remove-tag-path': 'Remove path for tag',
  'remove-tag-from-capi': 'Remove tag from CAPI',
  'remove-tag': 'Remove tag from Tag Manager',
  'add-tag-to-content': 'Add tag to content',
  'merge-tag-for-content': 'Merging tag in content',
  'reindex-tags': 'Reindexing tags',
  'reindex-sections': 'Reindexing sections'
};


export const prettyStepStatus = {
  'ready': 'Waiting',
  'processing': <ProgressSpinner/>,
  'processed': <ProgressSpinner/>,
  'complete': 'Complete',
  'rolledback': 'Reverted',
  'failed': 'Failed',
  'rollbackfailed': 'Revert Failed'
}

export const stepRowClass = {
  'failed': 'row-failed',
  'rollbackfailed': 'row-failed',
  'complete': 'row-complete',
  'rolledback': 'row-rolledback'
}
