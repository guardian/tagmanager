import React from 'react';
import R from 'ramda';
import {Link} from 'react-router';

import TagSelect from '../../../utils/TagSelect.js';
import SponsorEdit from '../../../SponsorshipEdit/SponsorEdit.react';
import ValidityEdit from '../../../SponsorshipEdit/ValidityEdit.react';

export default class PaidContentInfoEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  updatePaidContentSponsorship(updated) {
    this.props.updateTag(R.merge(this.props.tag, {
      sponsorship: updated
    }));
  }

  render() {

    const paidContentSponsorship = this.props.tag.sponsorship || {};

    return(
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Paid Content Information</label>
        <SponsorEdit sponsorship={paidContentSponsorship} updateSponsorship={this.updatePaidContentSponsorship.bind(this)}/>
        <ValidityEdit sponsorship={paidContentSponsorship} updateSponsorship={this.updatePaidContentSponsorship.bind(this)}/>
      </div>
    );
  }
}