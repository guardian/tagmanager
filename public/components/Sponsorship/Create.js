import React from 'react';
import SponsorshipTypeEdit from '../SponsorshipEdit/SponsorshipTypeEdit.react';
import SponsorEdit from '../SponsorshipEdit/SponsorEdit.react';
import ValidityEdit from '../SponsorshipEdit/ValidityEdit.react';
import TargetingEdit from '../SponsorshipEdit/TargetingEdit.react';
import SaveButton from '../utils/SaveButton.react';

class SponsorshipCreate extends React.Component {

    constructor(props) {
      super(props);

      this.isSponsorshipDirty = this.isSponsorshipDirty.bind(this);
    }

    componentDidMount() {
      this.props.sponsorshipActions.populateEmptySponsorship();

      if (!this.props.sections || !this.props.sections.length) {
        this.props.sectionActions.getSections();
      }
    }

    isSponsorshipDirty() {
      return this.props.saveState === 'SAVE_STATE_DIRTY';
    }

    isSponsorshipValid() {
      return this.props.sponsorship &&
        this.props.sponsorship.sponsorName &&
        this.props.sponsorship.sponsorLink &&
        //this.props.sponsorship.sponsorLogo &&
        (this.props.sponsorship.tag || this.props.sponsorship.section)
    }

    resetSponsorship() {
      this.props.sponsorshipActions.populateEmptySponsorship();
    }

    saveSponsorship() {
      this.props.sponsorshipActions.createSponsorship(this.props.sponsorship);
    }

    render () {

      return (
        <div className="sponsorship-edit">
          <div className="sponsorship-edit__column--sidebar">
            <SponsorshipTypeEdit sponsorship={this.props.sponsorship} updateSponsorship={this.props.sponsorshipActions.updateSponsorship}/>
            <SponsorEdit sponsorship={this.props.sponsorship} updateSponsorship={this.props.sponsorshipActions.updateSponsorship}/>
          </div>
          <div className="sponsorship-edit__column">
            <ValidityEdit sponsorship={this.props.sponsorship} updateSponsorship={this.props.sponsorshipActions.updateSponsorship}/>
          </div>
          <div className="sponsorship-edit__column">
            <TargetingEdit sponsorship={this.props.sponsorship} updateSponsorship={this.props.sponsorshipActions.updateSponsorship} sections={this.props.sections}/>
          </div>
          <SaveButton isHidden={!this.isSponsorshipValid() || !this.isSponsorshipDirty()} onSaveClick={this.saveSponsorship.bind(this)} onResetClick={this.resetSponsorship.bind(this)}/>
        </div>
      );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as createSponsorship from '../../actions/SponsorshipActions/createSponsorship';
import * as updateSponsorship from '../../actions/SponsorshipActions/updateSponsorship';
import * as getSections from '../../actions/SectionsActions/getSections';

function mapStateToProps(state) {
  return {
    config: state.config,
    saveState: state.saveState,
    sponsorship: state.sponsorship,
    sections: state.sections
  };
}

function mapDispatchToProps(dispatch) {
  return {
    sponsorshipActions: bindActionCreators(Object.assign({}, createSponsorship, updateSponsorship), dispatch),
    sectionActions: bindActionCreators(Object.assign({}, getSections), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(SponsorshipCreate);
