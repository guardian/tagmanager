import React from 'react';
import moment from 'moment';
import tagManagerApi from '../../util/tagManagerApi.js';

export default class JobStatus extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      audit: []
    };
  }

  componentDidMount() {
    this.fetchAudit();
  }

  fetchAudit() {
    tagManagerApi.getAuditForTag(this.props.tagId).then(res =>
        this.setState({audit: res})
    );
  }

  renderAuditRow(a) {
    return (
      <tr>
        <td>{a.date}</td>
        <td>{a.description}</td>
        <td>{a.user}</td>
      </tr>
    );
  }

  render () {
    return (
      <div className="job-status">
        <div className="job-status__header">Tag history</div>
          <table>
            <tbody>
            {this.state.audit.map(this.renderAuditRow, this)}
            </tbody>
          </table>
      </div>
    );
  }
}
