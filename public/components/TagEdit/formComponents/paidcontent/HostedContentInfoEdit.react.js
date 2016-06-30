import React from 'react';
import {PAID_HOSTEDCONTENT_TYPE} from '../../../../constants/paidContentTagTypes';

export default class HostedContentInfoEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  updateCampaignColour(e) {
    this.updatePaidContentInformation(Object.assign({}, this.props.paidContentInformation, {
      campaignColour: e.target.value
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
