import React from 'react';
import JobTable from './JobTable/JobTable.react';

import tagManagerApi from '../util/tagManagerApi';

class Status extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
          myJobs: [],
          allJobs: [],
        };
    }

    fetchJobs() {
      tagManagerApi.getAllJobs()
      .then((logs) => {
        const sortedJobs = logs.sort((a, b) => a.date > b.date ? -1 : 1);
        this.setState({
          myJobs: sortedJobs.filter( (job) => job.createdBy == this.props.config.username),
          allJobs: sortedJobs
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
          <div className="job__status--heading">Your jobs</div>
          <JobTable jobs={this.state.myJobs} simpleView={false} triggerRefresh={this.fetchJobs.bind(this)}  disableDelete={!this.props.config.permissions.tag_admin}/>
          <div className="job__status--heading">All jobs</div>
          <JobTable jobs={this.state.allJobs} simpleView={true} triggerRefresh={this.fetchJobs.bind(this)}  disableDelete={!this.props.config.permissions.tag_admin}/>
        </div>
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

export default connect(mapStateToProps)(Status);
