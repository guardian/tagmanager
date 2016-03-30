export function prettyJobStatus(jobStatus) {
  if (jobStatus === 'waiting' || jobStatus === 'owned') {
    // This is a pretty meaningless distinction to end users so just wrap it up as 'in progress'
    return 'In progress';
  } else if (jobStatus === 'complete') {
    return 'Done';
  } else if (jobStatus === 'failed') {
    return 'Failed';
  } else if (jobStatus === 'rolledback') {
    return 'Rolled back';
  }
  return jobStatus;
}

export function prettyStepType(stepType) {
  if (stepType === 'remove-tag-from-content') {
    return 'Remove tag from content';
  } else if (stepType === 'remove-tag-path') {
    return 'Remove path for tag';
  } else if (stepType === 'remove-tag-from-capi') {
    return 'Remove tag from CAPI';
  } else if (stepType === 'remove-tag') {
    return 'Remove tag from Tag Manager';
  } else if (stepType === 'add-tag-to-content') {
    return 'Add tag to content';
  } else if (stepType === 'merge-tag-for-content') {
    return 'Merging tag in content';
  } else if (stepType === 'reindex-tags') {
    return 'Reindexing tags';
  } else if (stepType === 'reindex-sections') {
    return 'Reindexing sections';
  }
  return stepType;
}

export function prettyStepStatus(stepStatus) {
  if (stepStatus === 'ready') {
    return 'Waiting';

  } else if (stepStatus === 'processing' || stepStatus === 'processed') {
    return <ProgressSpinner/>;

  } else if (stepStatus === 'complete') {
    return 'Complete';

  } else if (stepStatus === 'rolledback') {
    return 'Reverted';

  } else if (stepStatus === 'failed') {
    return 'Failed';

  } else if (stepStatus === 'rollbackfailed') {
    return 'Revert Failed';
  }
  return stepStatus;
}

export function stepRowClass(step) {
  if (step.stepStatus === 'failed' || step.stepStatus === 'rollbackfailed') {
    return 'row-failed';
  } else if (step.stepStatus === 'complete') {
    return 'row-complete';
  } else if (step.stepStatus === 'rolledback') {
    return 'row-rolledback';
  }
  return '';
}
