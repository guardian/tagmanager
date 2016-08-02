import React from 'react';
import {PAID_HOSTEDCONTENT_TYPE} from '../../../../constants/paidContentTagTypes';

export default class HostedContentInfoEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  updateCampaignColour(e) {

    let newColourValue = e.target.value;

    if (newColourValue && newColourValue[0] !== "#") {
      newColourValue = '#' + newColourValue;
    }

    this.props.updatePaidContentInformation(Object.assign({}, this.props.paidContentInformation, {
      campaignColour: newColourValue
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
          <input type="text" className="tag-edit__input" value={this.props.paidContentInformation.campaignColour} onChange={this.updateCampaignColour.bind(this)}/>
        </div>
      </div>
    );
  }
}
