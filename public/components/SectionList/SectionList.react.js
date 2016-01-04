import React from 'react';
import history from '../../routes/history';

class SectionList extends React.Component {

    constructor(props) {
        super(props);
    }

    onSectionClick(section) {
      history.replaceState(null, '/section/' + section.id);
    }

    componentDidMount() {

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
            {this.props.sections.sort((a, b) => a.name > b.name ? 1 : -1).map(this.renderListItem, this)}
          </tbody>
        </table>
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
