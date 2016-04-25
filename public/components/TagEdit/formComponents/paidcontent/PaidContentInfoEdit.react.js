import React from 'react';
import R from 'ramda';
import {Link} from 'react-router';

import TagSelect from '../../../utils/TagSelect.js';
import SectionSelect from '../../../utils/SectionSelect.react';
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

  onUpdateCreateMicrosite(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      createMicrosite: e.target.checked,
      section: undefined,
      capiSectionId: undefined
    }));
  }

  onUpdateSection(e) {

    const sectionId = parseInt(e.target.value, 10);
    const section = this.props.sections.filter((section) => section.id === sectionId)[0];

    this.props.updateTag(Object.assign({}, this.props.tag, {
      section: sectionId,
      capiSectionId: section.path
    }));
  }

  onUpdateIsMicrosite(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      isMicrosite: e.target.checked,
      section: undefined,
      capiSectionId: undefined
    }));
  }

  renderSectionControls() {
    if (this.props.tag.createMicrosite) {
      return (
        <div className="tag-edit__input-group" key="topic-section">
          <label className="tag-edit__input-group__header">Section</label>
          <input type="checkbox" checked={this.props.tag.createMicrosite} onChange={this.onUpdateCreateMicrosite.bind(this)} disabled={this.props.pathLocked || !this.props.tagEditable}/>
          <label className="tag-edit__label">create microsite for this tag</label>
        </div>
      )
    } else {
      return (
        <div className="tag-edit__input-group" key="topic-section">
          <label className="tag-edit__input-group__header">Section</label>
          <input type="checkbox" checked={this.props.tag.createMicrosite} onChange={this.onUpdateCreateMicrosite.bind(this)} disabled={this.props.pathLocked || !this.props.tagEditable}/>
          <label className="tag-edit__label">create microsite for this tag</label>
          <input type="checkbox" checked={this.props.tag.isMicrosite} onChange={this.onUpdateIsMicrosite.bind(this)} disabled={this.props.pathLocked || !this.props.tagEditable}/>
          <label className="tag-edit__label">is Microsite</label>
          <SectionSelect
            selectedId={this.props.tag.section}
            sections={this.props.sections}
            isMicrosite={this.props.tag.isMicrosite}
            onChange={this.onUpdateSection.bind(this)}
            disabled={this.props.pathLocked || !this.props.tagEditable}
            />
        </div>
      )
    }
  }

  render() {

    const paidContentSponsorship = this.props.tag.sponsorship || {};
    const selectPaidContentType = this.props.tag.paidContentInformation ? this.props.tag.paidContentInformation.paidContentType : undefined;

    return(
    <div>
      {this.renderSectionControls()}
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
    </div>
    );
  }
}