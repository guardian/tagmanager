import React from 'react';
import R from 'ramda';
import {Link} from 'react-router';

import TagSelect from '../../../utils/TagSelect.js';
import SponsorEdit from '../../../SponsorshipEdit/SponsorEdit.react';
import ValidityEdit from '../../../SponsorshipEdit/ValidityEdit.react';
import {paidContentTagTypes} from '../../../../constants/paidContentTagTypes';

export default class PaidContentInfoEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  updatePaidContentSponsorship(updated) {
    this.props.updateTag(R.merge(this.props.tag, {
      sponsorship: updated
    }));
  }

  updatePaidContentType(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      paidContentInformation: Object.assign({}, this.props.tag.paidContentInformation, {
        paidContentType: e.target.value
      })
    }));
  }

  render() {

    const paidContentSponsorship = this.props.tag.sponsorship || {};
    const selectPaidContentType = this.props.tag.paidContentInformation ? this.props.tag.paidContentInformation.paidContentType : undefined;

    return(
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Paid Content Information</label>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Paid Content Type</label>
          <select value={selectPaidContentType} onChange={this.updatePaidContentType.bind(this)} disabled={!this.props.tagEditable}>
            {!selectPaidContentType ? <option value={false}></option> : false}
            {paidContentTagTypes.sort((a, b) => {return a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1;}).map(function(t) {
              return (
                <option value={t.value} key={t.value} >{t.name}</option>
              );
            })}
          </select>
        </div>
        <SponsorEdit sponsorship={paidContentSponsorship} updateSponsorship={this.updatePaidContentSponsorship.bind(this)}/>
        <ValidityEdit sponsorship={paidContentSponsorship} updateSponsorship={this.updatePaidContentSponsorship.bind(this)}/>
      </div>
    );
  }
}