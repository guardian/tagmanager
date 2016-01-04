import React from 'react';
import SectionName from './SectionName.react';
import SaveButton from '../utils/SaveButton.react';

class SectionEdit extends React.Component {

    constructor(props) {
      super(props);

      this.isSectionDirty = this.isSectionDirty.bind(this);
    }

    componentDidMount() {
      if (!this.props.section || this.props.section.id !== parseInt(this.props.routeParams.sectionId, 10)) {
        this.props.sectionActions.getSection(this.props.routeParams.sectionId);
      }
    }

    isSectionDirty() {
      return this.props.saveState === 'SAVE_STATE_DIRTY';
    }

    resetSection() {
      this.props.sectionActions.getSection(this.props.routeParams.sectionId);
    }

    saveSection() {
      this.props.sectionActions.saveSection(this.props.section);

    }

    render () {

      if (!this.props.section || this.props.section.id !== parseInt(this.props.routeParams.sectionId, 10)) {
        return (
          <div>Fetching section...</div>
        );
      }

      return (
        <div className="section-edit">
          <div className="section-edit__column--sidebar">
            <SectionName section={this.props.section} updateSection={this.props.sectionActions.updateSection} />
          </div>
          <div className="section-edit__column">
          </div>
          <div className="section-edit__column">
          </div>

          <SaveButton isHidden={!this.isSectionDirty()} onSaveClick={this.saveSection.bind(this)} onResetClick={this.resetSection.bind(this)}/>

        </div>
      );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getSection from '../../actions/SectionsActions/getSection';
import * as updateSection from '../../actions/SectionsActions/updateSection';
import * as saveSection from '../../actions/SectionsActions/saveSection';

function mapStateToProps(state) {
  return {
    section: state.section,
    config: state.config,
    saveState: state.saveState
  };
}

function mapDispatchToProps(dispatch) {
  return {
    sectionActions: bindActionCreators(Object.assign({}, getSection, updateSection, saveSection), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(SectionEdit);
