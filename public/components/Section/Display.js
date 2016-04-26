import React from 'react';
import SectionEdit from '../SectionEdit/SectionEdit.react';
import SectionEdition from '../SectionEdit/SectionEditions.react';
import SaveButton from '../utils/SaveButton.react';
import UnexpireMicrosite from './UnexpireMicrosite.react';
import ExpireMicrosite from './ExpireMicrosite.react';

class SectionDisplay extends React.Component {

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

    updateEditions(editions) {
      this.props.sectionActions.updateSection(Object.assign({}, this.props.section, {
        editions: editions
      }));
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
            <SectionEdit section={this.props.section} updateSection={this.props.sectionActions.updateSection} pathLocked={true} />
            <UnexpireMicrosite section={this.props.section} showError={this.props.uiActions.showError}/>
            <ExpireMicrosite section={this.props.section} showError={this.props.uiActions.showError}/>
        </div>
          <div className="section-edit__column">
            <SectionEdition section={this.props.section} updateEditions={this.updateEditions.bind(this)} saveState={this.props.saveState} refreshSection={this.resetSection.bind(this)}/>
          </div>
          <div className="section-edit__column"></div>
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
import * as showError from '../../actions/UIActions/showError';

function mapStateToProps(state) {
  return {
    section: state.section,
    config: state.config,
    saveState: state.saveState
  };
}

function mapDispatchToProps(dispatch) {
  return {
    sectionActions: bindActionCreators(Object.assign({}, getSection, updateSection, saveSection), dispatch),
    uiActions: bindActionCreators(Object.assign({}, showError), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(SectionDisplay);
