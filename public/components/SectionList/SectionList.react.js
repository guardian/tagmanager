import React from 'react';
import {Link} from 'react-router-dom';
import { browserHistory } from '../../router'


class SectionList extends React.Component {

    constructor(props) {
        super(props);
    }

    onSectionClick(section) {
      const path = this.props.route.isMicrositeView ? '/microsite/' + section.id : '/section/' + section.id;
      browserHistory.push(path);
    }

    UNSAFE_componentWillMount() {
      if (!this.props.sections || !this.props.sections.length) {
        this.props.sectionActions.getSections();
      }
    }

    renderListItem(section) {

      const sectionClickHandler = (e) => {
        e.preventDefault();
        this.onSectionClick(section);
      };

      return (
        <a key={section.id}  href={this.props.route.isMicrositeView ? '/microsite/' + section.id : '/section/' + section.id} onClick={sectionClickHandler}>
          <div className="sectionlist__table__row" onClick={sectionClickHandler}>
            <div className="sectionlist__table__sectionName">{section.name}</div>
            <div className="sectionlist__table__sectionPath">{section.path}</div>
            <div className="sectionlist__table__sectionEditions">{Object.keys(section.editions).length + ' editions'}</div>
          </div>
        </a>
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
              <Link className="tag-search__create tag-search__create-button" to="/microsite/create">Create a new Microsite</Link> :
              <Link className="tag-search__create tag-search__create-button" to="/section/create">Create a new Section</Link>
            }
          </div>
          <div className="sectionlist__table" >
            <div className="sectionlist__table__row">
              <div className="sectionlist__table__sectionName--header">Name</div>
              <div className="sectionlist__table__sectionPath--header">Path</div>
              <div className="sectionlist__table__sectionEditions--header">Editionalised</div>
            </div>
            {sections.sort((a, b) => a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1).map(this.renderListItem, this)}
          </div>
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
