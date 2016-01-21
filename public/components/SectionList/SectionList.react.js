import React from 'react';
import history from '../../routes/history';

class SectionList extends React.Component {

    constructor(props) {
        super(props);
    }

    onSectionClick(section) {
      const path = this.props.route.isMicrositeView ? '/microsite/' + section.id : '/section/' + section.id;
      history.replaceState(null, path);
    }

    componentWillMount() {
      this.fetchSections();
    }

    componentWillReceiveProps(nextProps) {
      //Route Changed
      if (this.props.route.path !== nextProps.route.path) {
        this.fetchSections();
      }
    }

    fetchSections() {

      if (this.props.route.isMicrositeView) {
        if (!this.props.microsites || !this.props.microsites.length) {
          this.props.sectionActions.getMicrosites();
        }
        return;
      }

      if (!this.props.sections || !this.props.sections.length) {
        this.props.sectionActions.getSections();
      }
    }

    renderListItem(section) {
      return (
        <tr key={section.id} className="taglist__results-item" onClick={this.onSectionClick.bind(this, section)}>
          <td>{section.name}</td>
          <td>{section.path}</td>
          <td>{Object.keys(section.editions).length + ' editions'}</td>
        </tr>
      );
    }

    render () {

      const sections = this.props.route.isMicrositeView ? this.props.microsites : this.props.sections;

      if (!sections || !sections.length) {
        return (
          <div>Fetching sections...</div>
        );
      }

      return (
        <table className="sectionlist">
          <thead className="sectionlist__header">
            <tr>
              <th>Name</th>
              <th>Path</th>
              <th>Editionalised</th>
            </tr>
          </thead>
          <tbody className="sectionlist__results">
            {sections.sort((a, b) => a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1).map(this.renderListItem, this)}
          </tbody>
        </table>
      );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getSections from '../../actions/SectionsActions/getSections';
import * as getMicrosites from '../../actions/SectionsActions/getMicrosites';

function mapStateToProps(state) {
  return {
    sections: state.sections,
    microsites: state.microsites,
    config: state.config
  };
}

function mapDispatchToProps(dispatch) {
  return {
    sectionActions: bindActionCreators(Object.assign({}, getSections, getMicrosites), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(SectionList);
