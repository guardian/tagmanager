import React from 'react';
import moment from 'moment';
import ConfirmButton from '../utils/ConfirmButton.react';
import {prettyJobStatus, prettyStepType, prettyStepStatus, stepRowClass} from '../../constants/prettyJobLabels';
import {hasPermission} from '../../util/verifyPermission';

import tagManagerApi from '../../util/tagManagerApi';

class JobTable extends React.Component {

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

    renderJobStep(step, job) {
      return (
        <tr className={stepRowClass[step.stepStatus] ? stepRowClass[step.stepStatus] : ''} key={job.id + step.type}>
          <td>{prettyStepType[step.type] ? prettyStepType[step.type] : step.type }</td>
          <td>{step.stepMessage}</td>
          <td>{prettyStepStatus[step.stepStatus] ? prettyStepStatus[step.stepStatus] : step.stepStatus }</td>
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

      const rowClass = stepRowClass[step.stepStatus] ? stepRowClass[step.stepStatus] : '';

      return (
          <td key={job.id + step.type}>
            <span className={rowClass}>
              {prettyStepType[step.type] ? prettyStepType[step.type] : step.type }
            </span>
          </td>
        );
    }

    renderDeleteButton(job) {
      if ((job.jobStatus === 'failed' || job.jobStatus === 'rolledback' || job.jobStatus === 'complete') && (hasPermission('tag_admin') || job.createdBy === this.props.config.username)) {
        var text = 'Delete';
        var buttonClass = 'job__delete';

        if (job.jobStatus == 'complete' || job.jobStatus == 'rolledback') {
          text = 'Finished';
          buttonClass = 'job__delete--complete';
        }

        return <ConfirmButton className={buttonClass} buttonText={text} onClick={this.removeJob.bind(this, job.id)} disabled={this.props.disableDelete}/>;
      }
      return <ConfirmButton className='job__button--disabled' disabled={true} buttonText='Delete' />;
    }

    renderRollbackButton(job) {
      if (job.rollbackEnabled) {
        if ((job.jobStatus === 'failed' || job.jobStatus === 'complete')
          && (hasPermission('tag_admin') || job.createdBy === this.props.config.username)) {
          return <ConfirmButton className='job__rollback' buttonText='Rollback' onClick={this.rollbackJob.bind(this, job.id)} disabled={this.props.disableDelete}/>;
        }
        return <ConfirmButton className='job__button--disabled' disabled={true} buttonText='Rollback' />;
      }

      // Dont even show the rollback button on jobs which cannot be rolledback
      return false;
    }

    renderStatusCell(job) {
      return (<div>
        <div className='job__status'>{prettyJobStatus[job.jobStatus]}</div>
        <div>{this.renderDeleteButton(job)}</div>
        <div>{this.renderRollbackButton(job)}</div>
        </div>);
    }

    renderListItem(job) {
      const itemTime = moment(job.createdAt, 'x');

      return (
        <tbody className='jobtable__results' key={job.id}>
          <tr>
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
