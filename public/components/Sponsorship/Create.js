import React from 'react';
import SponsorshipTypeEdit from '../SponsorshipEdit/SponsorshipTypeEdit.react';
import SponsorEdit from '../SponsorshipEdit/SponsorEdit.react';
import ValidityEdit from '../SponsorshipEdit/ValidityEdit.react';
import TargetingEdit from '../SponsorshipEdit/TargetingEdit.react';
import ClashWarning from '../SponsorshipEdit/ClashWarning.react';
import SaveButton from '../utils/SaveButton.react';
import { Required } from '../SponsorshipEdit/Required.react';

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
        this.props.sponsorship.sponsorLogo &&
        this.hasTagOrSection(this.props.sponsorship) &&
        (this.props.clashingSponsorships && this.props.clashingSponsorships.length == 0)
    }

    hasTagOrSection(sponsorship) {
      return !!(sponsorship.tags && sponsorship.tags.length) ||
          !!(sponsorship.sections && sponsorship.sections.length)
    }

    hasClashingSponsorships() {
      return !!(this.props.clashingSponsorships && this.props.clashingSponsorships.length)
    }

    resetSponsorship() {
      this.props.sponsorshipActions.populateEmptySponsorship();
    }

    saveSponsorship() {
      this.props.sponsorshipActions.createSponsorship(this.props.sponsorship);
    }

    updateSponsorshipAndCheckClashes(sponsorship) {
      this.props.sponsorshipActions.updateSponsorship(sponsorship);
      if(this.hasTagOrSection(sponsorship) || this.hasClashingSponsorships()) {
        this.props.sponsorshipActions.getClashingSponsorships(sponsorship);
      }
    }

    render () {

      return (
        <div className="sponsorship-edit">
          <div className="sponsorship-edit__column--sidebar">
            <SponsorshipTypeEdit sponsorship={this.props.sponsorship} updateSponsorship={this.props.sponsorshipActions.updateSponsorship}/>
            <SponsorEdit sponsorship={this.props.sponsorship} updateSponsorship={this.props.sponsorshipActions.updateSponsorship}/>
          </div>
          <div className="sponsorship-edit__column">
            <ValidityEdit sponsorship={this.props.sponsorship} updateSponsorship={this.updateSponsorshipAndCheckClashes.bind(this)} />
          </div>
          <div className="sponsorship-edit__column">
            <TargetingEdit sponsorship={this.props.sponsorship} updateSponsorship={this.updateSponsorshipAndCheckClashes.bind(this)} sections={this.props.sections} />
            <ClashWarning clashingSponsorships={this.props.clashingSponsorships} />
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
import * as getClashingSponsorships from '../../actions/SponsorshipActions/getClashingSponsorships.js';
import * as getSections from '../../actions/SectionsActions/getSections';

function mapStateToProps(state) {
  return {
    config: state.config,
    saveState: state.saveState,
    sponsorship: state.sponsorship,
    clashingSponsorships: state.clashingSponsorships,
    sections: state.sections
  };
}

function mapDispatchToProps(dispatch) {
  return {
    sponsorshipActions: bindActionCreators(Object.assign({}, createSponsorship, updateSponsorship, getClashingSponsorships), dispatch),
    sectionActions: bindActionCreators(Object.assign({}, getSections), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(SponsorshipCreate);
