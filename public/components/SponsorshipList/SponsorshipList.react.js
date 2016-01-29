import React from 'react';
import history from '../../routes/history';
import {Link} from 'react-router';

class SponsorshipsList extends React.Component {

    constructor(props) {
        super(props);
    }


    render () {

      if (!this.props.sponsorships || !this.props.sponsorships.length) {
        return (
          <div>Fetching sponsorships...</div>
        );
      }

      const sponsorships = this.props.sponsorships;

      return (
        <div className="sponsorshiplist">
          Hello from sponsorships list
        </div>

      );
    }
}

//REDUX CONNECTIONS
//import { connect } from 'react-redux';
//import { bindActionCreators } from 'redux';
//import * as getSponsorships from '../../actions/SponsorshipActions/getSponsorships';
//
//function mapStateToProps(state) {
//  return {
//    sponsorships: state.sponsorships,
//    config: state.config
//  };
//}
//
//function mapDispatchToProps(dispatch) {
//  return {
//    sponsorshipActions: bindActionCreators(Object.assign({}, getSponsorships), dispatch)
//  };
//}
//
//export default connect(mapStateToProps, mapDispatchToProps)(SponsorshipsList);
