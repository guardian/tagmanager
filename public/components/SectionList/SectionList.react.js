import React from 'react';
import history from '../../routes/history';
import {Link} from 'react-router';

class SectionList extends React.Component {

    constructor(props) {
        super(props);
    }

    onSectionClick(section) {
      const path = this.props.route.isMicrositeView ? '/microsite/' + section.id : '/section/' + section.id;
      history.replaceState(null, path);
    }

    componentWillMount() {
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

      if (!this.props.sections || !this.props.sections.length) {
        return (
          <div>Fetching sections...</div>
        );
      }

      const sections = this.props.route.isMicrositeView ? this.props.sections.filter(sec => sec.isMicrosite === true) : this.props.sections.filter(sec => sec.isMicrosite === false);

      return (
        <div className="sectionlist">
          <div className="tag-search__filters">
            {this.props.route.isMicrositeView ?
              <Link className="tag-search__create" to="/microsite/create">Create a new Microsite</Link> :
              <Link className="tag-search__create" to="/section/create">Create a new Section</Link>
            }

          </div>
          <table >
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
        </div>

      );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getSections from '../../actions/SectionsActions/getSections';

function mapStateToProps(state) {
  return {
    sections: state.sections,
    config: state.config
  };
}

function mapDispatchToProps(dispatch) {
  return {
    sectionActions: bindActionCreators(Object.assign({}, getSections), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(SectionList);
