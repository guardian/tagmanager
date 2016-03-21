import React from 'react';
import moment from 'moment';
import ConfirmButton from '../utils/ConfirmButton.react';
import ProgressSpinner from '../utils/ProgressSpinner.react';
import {hasPermission} from '../../util/verifyPermission';

import tagManagerApi from '../../util/tagManagerApi';

export default class JobTable extends React.Component {

    constructor(props) {
        super(props);
    }

    removeJob(jobId) {
      var self = this;
      tagManagerApi.deleteJob(jobId).then((res) => {
        setTimeout(() => {
          self.props.triggerRefresh();
        }, 1000);
      });
    }

    rollbackJob(jobId) {
      var self = this;
      tagManagerApi.rollbackJob(jobId).then((res) => {
        setTimeout(() => {
          self.props.triggerRefresh();
        }, 1000);
      });
    }

    prettyJobStatus(jobStatus) {
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

    prettyStepType(stepType) {
      if (stepType === 'remove-tag-from-content') {
        return 'Remove tag from all content';
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

    prettyStepStatus(stepStatus) {
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

    stepRowClass(step) {
      if (step.stepStatus === 'failed' || step.stepStatus === 'rollbackfailed') {
        return 'row-failed';
      } else if (step.stepStatus === 'complete') {
        return 'row-complete';
      } else if (step.stepStatus === 'rolledback') {
        return 'row-rolledback';
      }
      return '';
    }

    renderJobStep(step, job) {
      return (
        <tr className={this.stepRowClass(step)} key={job.id + step.type}>
          <td>{this.prettyStepType(step.type)}</td>
          <td>{step.stepMessage}</td>
          <td>{this.prettyStepStatus(step.stepStatus)}</td>
        </tr>);
    }

    renderAllSteps(job) {
      return (
        <td>
          <table className="grid-table jobtable">
            <thead>
              <tr>
                <th>Step</th>
                <th>Message</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
            {job.steps.map(this.renderJobStep, this, job)}
            </tbody>
          </table>
        </td>
      );
    }

    renderCurrentStep(job) {
      const step = job.steps.find(s => {
          return s.stepStatus !== 'complete';
      });

      if (!step) {
        return (
          <td>
            <span className='row-complete'>
              Job done
            </span>
          </td>
        );
      }

      const rowClass = this.stepRowClass(step);
      return (
          <td>
            <span className={rowClass}>
              {this.prettyStepType(step.type)}
            </span>
          </td>
        );
    }

    renderDeleteButton(job) {
      if ((job.jobStatus === 'failed' || job.jobStatus === 'rolledback' || job.jobStatus === 'complete') && (hasPermission('tag_admin') || job.createdBy === this.props.config.username)) {
        return <ConfirmButton className='job__delete' buttonText='Delete' onClick={this.removeJob.bind(this, job.id)} disabled={this.props.disableDelete}/>;
      }
      return <ConfirmButton className='job__button--disabled' disabled={true} buttonText='Delete' />;
    }

    renderRollbackButton(job) {
      if (job.rollbackEnabled
        && (job.jobStatus === 'failed' || job.jobStatus === 'complete')
        && (hasPermission('tag_admin') || job.createdBy === this.props.config.username)) {
        return <ConfirmButton className='job__rollback' buttonText='Rollback' onClick={this.rollbackJob.bind(this, job.id)} disabled={this.props.disableDelete}/>;
      }
      return <ConfirmButton className='job__button--disabled' disabled={true} buttonText='Rollback' />;
    }

    renderStatusCell(job) {
      return (<div>
        <div className='job__status'>{this.prettyJobStatus(job.jobStatus)}</div>
        <div>{this.renderDeleteButton(job)}</div>
        <div>{this.renderRollbackButton(job)}</div>
        </div>);
    }

    renderListItem(job) {
      const itemTime = moment(job.createdAt, 'x');

      return (
        <tbody className='jobtable__results'>
          <tr key={job.id}>
            <td>
              {job.title}<br />
              {itemTime.format('DD/MM/YYYY')}<br />
              {itemTime.format('HH:mm:ss')}
            </td>
            <td>{job.createdBy}</td>
              {this.props.simpleView ? this.renderCurrentStep(job) : this.renderAllSteps(job)}
            <td>
              {this.renderStatusCell(job)}
            </td>
          </tr>
        </tbody>
      );
    }

    render () {
      return (
          <table className='grid-table jobtable'>
            <thead className='jobtable__header'>
              <tr>
                <th>Job</th>
                <th>User</th>
                <th>
                  {this.props.simpleView ? 'Current Step' : 'Progress'}
                </th>
                <th>Status</th>
              </tr>
            </thead>
            {this.props.jobs.map(this.renderListItem, this)}
          </table>

      );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';

function mapStateToProps(state) {
  return {
    config: state.config
  };
}

export default connect(mapStateToProps)(JobTable);
