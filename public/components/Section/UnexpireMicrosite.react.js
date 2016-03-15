import React from 'react';
import tagManagerApi from '../../util/tagManagerApi.js';
import {hasPermission} from '../../util/verifyPermission';
import ConfirmButton from '../utils/ConfirmButton.react';

export default class UnexpireMicrosite extends React.Component {

    constructor(props) {
      super(props);

      this.state = {
        unexpiryTriggered: false
      };
    }

    triggerUnexpiry() {
      this.setState({
        unexpiryTriggered: true
      });

      tagManagerApi.unexpireContentForSection(this.props.section.id).then((res) => {
        this.setState({
          unexpiryTriggered: false
        });
      }).catch((err) => {
        this.props.showError('Could not trigger Unexpiry');
        console.error('Error unexpiring microsite',  err);
      });
    }

    render () {

      if (!this.props.section || this.props.section.isMicrosite !== true || !hasPermission('tag_admin')) {
        return false;
      }

      return (
        <div className="section__unexpiry">
          <ConfirmButton disabled={this.state.unexpiryTriggered} onClick={this.triggerUnexpiry.bind(this)} buttonText="Unexpire Microsite"/>
        </div>
      );

    }
}
