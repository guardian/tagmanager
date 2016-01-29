import React from 'react';
import SponsorEdit from '../SponsorshipEdit/SponsorEdit.react';
import ValidityEdit from '../SponsorshipEdit/ValidityEdit.react';
import TargetingEdit from '../SponsorshipEdit/TargetingEdit.react';
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
    }

    isSponsorshipDirty() {
      return this.props.saveState === 'SAVE_STATE_DIRTY';
    }

    resetSponsorship() {
      this.props.sponsorshipActions.getSponsorship(this.props.routeParams.sponsorshipId);
    }

    saveSponsorship() {
      this.props.sponsorshipActions.saveSponsorship(this.props.sponsorship);
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
            <ValidityEdit sponsorship={this.props.sponsorship} updateSponsorship={this.props.sponsorshipActions.updateSponsorship}/>
          </div>
          <div className="sponsorship-edit__column">
            <TargetingEdit sponsorship={this.props.sponsorship} updateSponsorship={this.props.sponsorshipActions.updateSponsorship}/>
          </div>
          <SaveButton isHidden={!this.isSponsorshipDirty()} onSaveClick={this.saveSponsorship.bind(this)} onResetClick={this.resetSponsorship.bind(this)}/>
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

function mapStateToProps(state) {
  return {
    sponsorship: state.sponsorship,
    config: state.config,
    saveState: state.saveState
  };
}

function mapDispatchToProps(dispatch) {
  return {
    sponsorshipActions: bindActionCreators(Object.assign({}, getSponsorship, updateSponsorship, saveSponsorship), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(SponsorshipDisplay);
