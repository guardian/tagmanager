import React from 'react';
import tagManagerApi from '../../util/tagManagerApi.js';
import {hasPermission} from '../../util/verifyPermission';
import ConfirmButton from '../utils/ConfirmButton.react';

export default class ExpireMicrosite extends React.Component {

    constructor(props) {
      super(props);

      this.state = {
        expiryTriggered: false
      };
    }

    triggerExpiry() {
      this.setState({
          expiryTriggered: true
      });

      tagManagerApi.expireContentForSection(this.props.section.id).then((res) => {
        this.setState({
          expiryTriggered: false
        });
      }).catch((err) => {
        this.props.showError('Could not trigger Expiry');
        console.error('Error expiring microsite',  err);
      });
    }

    render () {

      if (!this.props.section || this.props.section.isMicrosite !== true || !hasPermission('tag_admin')) {
        return false;
      }

      return (
        <div className="section__unexpiry">
          <ConfirmButton className="section__unexpiry__button" disabled={this.state.expiryTriggered} onClick={this.triggerExpiry.bind(this)} buttonText="Expire Microsite"/>
        </div>
      );

    }
}
