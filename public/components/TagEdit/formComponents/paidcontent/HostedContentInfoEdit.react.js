import React from 'react';
import { ChromePicker } from 'react-color';
import ColourPicker from '../../../utils/ColourPicker';

import {PAID_HOSTEDCONTENT_TYPE} from '../../../../constants/paidContentTagTypes';

export default class HostedContentInfoEdit extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      showColourPicker: false
    };
  }

  updateCampaignColour(color) {
    this.props.updatePaidContentInformation(Object.assign({}, this.props.paidContentInformation, {
      campaignColour: color
    }));
  }

  render() {

    if (!this.props.paidContentInformation || this.props.paidContentInformation.paidContentType !== PAID_HOSTEDCONTENT_TYPE.value) {
      return false;
    }

    return (
      <div className="tag-edit__input-group">
        <div className="tag-edit__field">
          <label className="tag-edit__label">Hosted Campaign Colour (Hex Code)</label>
          <ColourPicker
            value={this.props.paidContentInformation.campaignColour}
            onChange={this.updateCampaignColour.bind(this)}
          />
        </div>
      </div>
    );
  }
}
