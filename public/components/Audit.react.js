import React from 'react';
import {allowedAuditReports} from '../constants/allowedAuditReports';
import tagManagerApi from '../util/tagManagerApi';
import moment from 'moment';
import ReactTooltip from 'react-tooltip';

const reportSubjects = ['tag', 'section'];

export default class Audit extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
          selectedReport: allowedAuditReports[0],
          selectedSubject: reportSubjects[0]
        };
    }

    fetchAuditLogs(newState) {
      const state = newState || this.state;
      const updateFn = state.selectedSubject === 'tag' ? tagManagerApi.getAuditForTagOperation : tagManagerApi.getAuditForSectionOperation;

      updateFn(state.selectedReport)
      .then((logs) => {
        this.setState({
          auditLog: logs.sort((a, b) => a.date > b.date ? -1 : 1)
        });
      });
    }

    componentDidMount() {
      if (!this.state.auditLog || !this.state.auditLog.length) {
        this.fetchAuditLogs();
      }
    }

    renderListItem(logItem) {
      const date = moment.unix(logItem.date).format('ddd DD MMM HH:mm')
      const summary = Object.keys(logItem.tagSummary).map(k => `${k}: ${logItem.tagSummary[k]}`).join('<br />')

      return (
        <tr key={logItem.operation + logItem.date} className="taglist__results-item">
          <td>{date}</td>
          <td>{logItem.operation}</td>
          <td>{logItem.description}</td>
          <td>{logItem.user}</td>
          <td className="taglist__results-info" data-tip={summary}><i className="i-info-grey" /></td>
        </tr>
      );
    }

    selectReportType(reportName) {

      const newState = Object.assign({}, this.state, {selectedReport: reportName});
      this.setState(newState);

      //as state may not be set, need to pass it through :(
      this.fetchAuditLogs(newState);

    }

    selectReportSubject(subjectName) {
      const newState = Object.assign({}, this.state, {selectedSubject: subjectName});
      this.setState(newState);

      //as state may not be set, need to pass it through :(
      this.fetchAuditLogs(newState);

    }

    renderTable() {

      if (!this.state.auditLog) {
        return (
          <div>Fetching log...</div>
        );
      }

      return (
        <table className="audit__table">
          <thead className="audit__header">
            <tr>
              <th>Date</th>
              <th>Operation</th>
              <th>Desciption</th>
              <th>User</th>
              <th></th>
            </tr>
          </thead>
          <tbody className="audit__results">
            {this.state.auditLog.sort((a, b) => a.name > b.name ? 1 : -1).map(this.renderListItem, this)}
          </tbody>
        </table>
      );
    }

    render () {

      return (

        <div className="audit">
          <div className="audit__tabs">
            {reportSubjects.map((subject) => {
              const tabClass = this.state.selectedSubject === subject ? 'audit__tab--selected' : 'audit__tab';
              return (
                <div key={subject} className={tabClass} onClick={this.selectReportSubject.bind(this, subject)}>
                  {subject}
                </div>
              );
            }, this)}
          </div>
          <div className="audit__tabs">
            {allowedAuditReports.map((reportName) => {
              const tabClass = this.state.selectedReport === reportName ? 'audit__tab--selected' : 'audit__tab';
              return (
                <div key={reportName} className={tabClass} onClick={this.selectReportType.bind(this, reportName)}>
                  {reportName}
                </div>
              );
            }, this)}
          </div>
          {this.renderTable()}
          <ReactTooltip multiline={true} place="right" effect="solid"/>
        </div>

      );
    }
}
