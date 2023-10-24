import React from 'react';
import moment from 'moment';
import tagManagerApi from '../../util/tagManagerApi.js';

export default class TagAudit extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      audit: []
    };
  }

  componentDidMount() {
    this.fetchAudit();
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    if(this.props.saveState === 'SAVE_STATE_INPROGRESS' && nextProps.saveState === 'SAVE_STATE_CLEAN') { //I.E a save just completed
      this.fetchAudit();
    }
  }

  fetchAudit() {
    tagManagerApi.getAuditForTag(this.props.tagId).then(res =>
      this.setState({audit: res})
    );
  }

  renderAuditRow(a) {
    return (
      <tr key={a.date}>
        <td>{moment(a.date).format('DD/MM/YYYY HH:mm:ss')}</td>
        <td>{a.description}</td>
        <td>{a.user}</td>
      </tr>
    );
  }

  render () {
    return (
      <div className="tag-audit">
        <div className="tag-audit__header">Tag history</div>
          <table className="grid-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Event</th>
                <th>User</th>
              </tr>
            </thead>
            <tbody>
            {this.state.audit.sort((a,b) => b.date - a.date).map(this.renderAuditRow, this)}
            </tbody>
          </table>
      </div>
    );
  }
}
