import React from 'react';
import SponsorEdit from '../SponsorshipEdit/SponsorEdit.react';
import ValidityEdit from '../SponsorshipEdit/ValidityEdit.react';
import TargetingEdit from '../SponsorshipEdit/TargetingEdit.react';
import ClashWarning from '../SponsorshipEdit/ClashWarning.react';
import SaveButton from '../utils/SaveButton.react';

class SponsorshipDisplay extends React.Component {

    constructor(props) {
      super(props);

      this.isSponsorshipDirty = this.isSponsorshipDirty.bind(this);
    }

    componentDidMount() {
      if (!this.props.sponsorship || this.props.sponsorship.id !== parseInt(this.props.routeParams.sponsorshipId, 10)) {
        this.props.sponsorshipActions.getSponsorship(this.props.routeParams.sponsorshipId);
      }

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
      this.props.sponsorshipActions.getSponsorship(this.props.routeParams.sponsorshipId);
    }

    saveSponsorship() {
      this.props.sponsorshipActions.saveSponsorship(this.props.sponsorship);
    }

    updateSponsorshipAndCheckClashes(sponsorship) {
      this.props.sponsorshipActions.updateSponsorship(sponsorship);
      if(this.hasTagOrSection(sponsorship) || this.hasClashingSponsorships()) {
        this.props.sponsorshipActions.getClashingSponsorships(sponsorship);
      }
    }

    render () {

      if (!this.props.sponsorship || this.props.sponsorship.id !== parseInt(this.props.routeParams.sponsorshipId, 10)) {
        return (
          <div>Fetching sponsorship...</div>
        );
      }

      return (
        <div className="sponsorship-edit">
          <div className="sponsorship-edit__column--sidebar">
            <SponsorEdit sponsorship={this.props.sponsorship} updateSponsorship={this.props.sponsorshipActions.updateSponsorship}/>
          </div>
          <div className="sponsorship-edit__column">
            <div className="tag-edit__input-group">
              <label className="tag-edit__input-group__header">Status</label>
              <div className="tag-edit__field" >{this.props.sponsorship.status}</div>
            </div>
            <ValidityEdit sponsorship={this.props.sponsorship} updateSponsorship={this.updateSponsorshipAndCheckClashes.bind(this)}/>
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
import * as getSponsorship from '../../actions/SponsorshipActions/getSponsorship';
import * as updateSponsorship from '../../actions/SponsorshipActions/updateSponsorship';
import * as saveSponsorship from '../../actions/SponsorshipActions/saveSponsorship';
import * as getClashingSponsorships from '../../actions/SponsorshipActions/getClashingSponsorships.js';
import * as getSections from '../../actions/SectionsActions/getSections';

function mapStateToProps(state) {
  return {
    sponsorship: state.sponsorship,
    config: state.config,
    saveState: state.saveState,
    clashingSponsorships: state.clashingSponsorships,
    sections: state.sections
  };
}

function mapDispatchToProps(dispatch) {
  return {
    sponsorshipActions: bindActionCreators(Object.assign({}, getSponsorship, updateSponsorship, saveSponsorship, getClashingSponsorships), dispatch),
    sectionActions: bindActionCreators(Object.assign({}, getSections), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(SponsorshipDisplay);
