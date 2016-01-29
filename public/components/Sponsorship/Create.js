import React from 'react';
import SponsorshipEdit from '../SponsorshipEdit/SponsorshipEdit.react';
import SaveButton from '../utils/SaveButton.react';

class SponsorshipCreate extends React.Component {

    constructor(props) {
      super(props);

      this.isSponsorshipDirty = this.isSponsorshipDirty.bind(this);
    }

    componentDidMount() {
      this.props.sponsorshipActions.populateEmptySponsorship();
    }

    isSponsorshipDirty() {
      return this.props.saveState === 'SAVE_STATE_DIRTY';
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
            <SponsorshipEdit sponsorship={this.props.sponsorship} updateSponsorship={this.props.sponsorshipActions.updateSponsorship}/>
          </div>
          <div className="section-edit__column"></div>
          <div className="section-edit__column"></div>
          <SaveButton isHidden={!this.isSponsorshipDirty()} onSaveClick={this.saveSponsorship.bind(this)} onResetClick={this.resetSponsorship.bind(this)}/>
        </div>
      );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as createSponsorship from '../../actions/SponsorshipActions/createSponsorship';
import * as updateSponsorship from '../../actions/SponsorshipActions/updateSponsorship';

function mapStateToProps(state) {
  return {
    config: state.config,
    saveState: state.saveState,
    sponsorship: state.sponsorship
  };
}

function mapDispatchToProps(dispatch) {
  return {
    sponsorshipActions: bindActionCreators(Object.assign({}, createSponsorship, updateSponsorship), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(SponsorshipCreate);
