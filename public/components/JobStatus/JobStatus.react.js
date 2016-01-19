import React from 'react';
import moment from 'moment';
import tagManagerApi from '../../util/tagManagerApi.js';

export default class JobStatus extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      jobs: []
    };

  }

  componentDidMount() {
    this.fetchJobs();

    const intervalId = setInterval(this.fetchJobs.bind(this), 5000);
    this.setState({jobCheckInterval: intervalId});
  }

  componentWillUnmount() {
    clearInterval(this.state.jobCheckInterval);
  }

  fetchJobs() {

    if(this.props.tagId) {
      tagManagerApi.getJobsByTag(this.props.tagId).then(res =>
        this.setState({jobs: res})
      )
    } else {
      tagManagerApi.getAllJobs().then(res => {
        this.setState({jobs: res});
      });
    }
  }

  describeCommand(job) {
    const commandType = job.command.type;

    if(commandType === 'BatchTagCommand') {
      const op = job.command.operation;
      if(op === 'remove') {
        return 'Removing tag from content';
      } else if (op === 'addToTop') {
        return 'Adding tag to the top of content';
      } else if (op === 'addToBottom') {
        return 'Adding tag to the bottom of content';
      } else {
        return 'Unexpected batch tag operation';
      }
    } else {
      return job.type;
    }
  }

  describeCurrentStep(job) {
    const step = job.steps[0];
    const stepType = step.type;

    if (stepType === 'BatchTagRemoveCompleteCheck' || stepType === 'BatchTagAddCompleteCheck') {
      return 'completed ' + step.completed + ' of ' + step.contentIds.length;
    } else {
      return stepType;
    }
  }

  describeJob(job) {
    return (
      <tr className="job-status__job" key={job.id} >
        <td className="job-status__job__progress">{this.describeCommand(job)}: {this.describeCurrentStep(job)}</td>
      </tr>
    );
  }

  render () {
    const self = this;
    return (
      <table className="job-status">
        <thead>
          <tr>
            <th>Currently running jobs</th>
          </tr>
        </thead>
        <tbody>
          {this.state.jobs.map(function (job) {
            return self.describeJob(job);
          })}
        </tbody>
      </table>
    );
  }
}
