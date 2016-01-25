import React from 'react';
import SectionEdit from '../SectionEdit/SectionEdit.react';
import SaveButton from '../utils/SaveButton.react';

class SectionCreate extends React.Component {

    constructor(props) {
      super(props);

      this.isSectionDirty = this.isSectionDirty.bind(this);

      this.state = {
        pathInUse: false
      };
    }

    componentDidMount() {
      this.props.sectionActions.populateEmptySection(this.props.route.isMicrositeView);
    }

    isSectionDirty() {
      return this.props.saveState === 'SAVE_STATE_DIRTY';
    }

    resetSection() {
      this.props.sectionActions.populateEmptySection(this.props.route.isMicrositeView);
    }

    saveSection() {
      this.props.sectionActions.createSection(this.props.section);
    }

    render () {

      return (
        <div className="section-edit">
          <div className="section-edit__column--sidebar">
            <SectionEdit section={this.props.section} updateSection={this.props.sectionActions.updateSection}/>
          </div>
          <div className="section-edit__column"></div>
          <div className="section-edit__column"></div>
          <SaveButton isHidden={!this.isSectionDirty()} onSaveClick={this.saveSection.bind(this)} onResetClick={this.resetSection.bind(this)}/>
        </div>
      );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as createSection from '../../actions/SectionsActions/createSection';
import * as updateSection from '../../actions/SectionsActions/updateSection';

function mapStateToProps(state) {
  return {
    config: state.config,
    saveState: state.saveState,
    section: state.section
  };
}

function mapDispatchToProps(dispatch) {
  return {
    sectionActions: bindActionCreators(Object.assign({}, createSection, updateSection), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(SectionCreate);
