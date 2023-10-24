import React from 'react';
import tagManagerApi from '../../util/tagManagerApi.js';
import JobTable from '../JobTable/JobTable.react';

export default class JobStatus extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      jobs: []
    };

  }

  componentDidMount() {
    this.fetchJobs();

    this.jobCheck = setInterval(this.fetchJobs.bind(this), 5000);
  }

  UNSAFE_componentWillUnmount() {
    clearInterval(this.jobCheck);
  }

  fetchJobs() {
    if (this.props.tagId) {
      tagManagerApi.getJobsByTag(this.props.tagId).then(res =>
        this.setState({jobs: res})
      );
    }
  }

  render () {
    return (
      <JobTable jobs={this.state.jobs} simpleView={true} triggerRefresh={this.fetchJobs.bind(this)} disableDelete={!this.props.config.permissions.tag_admin}/>
    );
  }
}
