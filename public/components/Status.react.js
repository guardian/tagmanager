import React from 'react';
import tagManagerApi from '../util/tagManagerApi';
import moment from 'moment';

export default class Status extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
          jobStatus: []
        };
    }

    fetchJobStatus() {
      tagManagerApi.getAllJobs()
      .then((logs) => {
        this.setState({
          jobStatus: logs.sort((a, b) => a.date > b.date ? -1 : 1)
        });
      });
    }

    componentDidMount() {
      if (!this.state.jobStatus || !this.state.jobStatus.length) {
        this.fetchJobStatus();
      }
    }

    renderListItem(logItem) {

      const itemTime = moment(logItem.started, 'x');
      const rowClass = moment().subtract(1, 'days').isAfter(itemTime) ? 'row-warning' : '';

      return (
        <tr className={rowClass} key={logItem.id}>
          <td>{itemTime.format('DD/MM/YYYY HH:mm:ss')}</td>
          <td>{logItem.type}</td>
          <td>{logItem.startedBy}</td>
          <td>Currently Processing: {logItem.steps[0].type}<br />Remaining Steps: {logItem.steps.length}</td>
        </tr>
      );
    }

    render () {

      return (

        <div className="status">
          <table className="status__table">
            <thead className="status__header">
              <tr>
                <th>Started</th>
                <th>Type</th>
                <th>User</th>
                <th>Progress</th>
              </tr>
            </thead>
            <tbody className="status__results">
              {this.state.jobStatus.sort((a, b) => a.id > b.id ? 1 : -1).map(this.renderListItem, this)}
            </tbody>
          </table>
        </div>

      );
    }
}
