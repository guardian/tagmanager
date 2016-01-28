import React from 'react';
import JobTable from './JobTable/JobTable.react';

import tagManagerApi from '../util/tagManagerApi';

export default class Status extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
          jobStatus: []
        };
    }

    fetchJobs() {
      tagManagerApi.getAllJobs()
      .then((logs) => {
        this.setState({
          jobStatus: logs.sort((a, b) => a.date > b.date ? -1 : 1)
        });
      });
    }

    componentDidMount() {
      this.fetchJobs();
      this.jobCheck = setInterval(this.fetchJobs.bind(this), 15000);
    }

    componentWillUnmount() {
      clearInterval(this.jobCheck);
    }

    render () {
      return (
        <div className="status">
          <JobTable jobs={this.state.jobStatus} triggerRefresh={this.fetchJobs.bind(this)} />
        </div>
      );
    }
}
