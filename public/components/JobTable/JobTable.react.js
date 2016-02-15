import React from 'react';
import moment from 'moment';
import {Link} from 'react-router';
import ConfirmButton from '../utils/ConfirmButton.react';

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

    stepProgress(step) {
      if (step.type === 'AllUsagesOfTagRemovedCheck') {
        return (
          <span>
            <b>{(step.originalCount - step.completed)}/{step.originalCount}</b> content still contain <b>{step.apiTagId}</b>
          </span>
        );
      }

      if (step.type === 'TagRemovedCheck') {
        return (
          <span>
            Confirming <b>{step.apiTagId}</b> has been removed from CAPI
          </span>
        );
      }

      return false;
    }

    renderListItem(job) {

      const itemTime = moment(job.started, 'x');
      const rowClass = moment().subtract(1, 'hour').isAfter(itemTime) ? 'row-warning' : '';

      return (
        <tr className={rowClass} key={job.id}>
          <td>
            {itemTime.format('DD/MM/YYYY')}<br />
            {itemTime.format('HH:mm:ss')}
          </td>
          <td>{job.type}</td>
          <td>
            {job.tagIds.map((tagId) => {
                return (
                  <div key={tagId}>
                    <Link to={'/tag/' + tagId}>{tagId}</Link>
                  </div>
                );
            })}
          </td>
          <td>{job.startedBy}</td>
          <td>
            Currently Processing: {job.steps[0].type}<br />
            {this.stepProgress(job.steps[0])}<br />
            Remaining Steps: {job.steps.length}
          </td>
          <td><ConfirmButton buttonText="Delete Job" onClick={this.removeJob.bind(this, job.id)} disabled={this.props.disableDelete}/></td>
        </tr>
      );
    }

    render () {

      return (
          <table className="grid-table jobtable">
            <thead className="jobtable__header">
              <tr>
                <th>Started</th>
                <th>Type</th>
                <th>Tags</th>
                <th>User</th>
                <th>Progress</th>
                <th></th>
              </tr>
            </thead>
            <tbody className="jobtable__results">
              {this.props.jobs.sort((a, b) => a.started < b.started ? 1 : -1).map(this.renderListItem, this)}
            </tbody>
          </table>

      );
    }
}
