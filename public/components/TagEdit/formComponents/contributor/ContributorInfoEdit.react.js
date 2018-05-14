
import React from 'react';
import R from 'ramda';
import ReactTooltip from 'react-tooltip'

import TagImageEdit from '../TagImageEdit.react';

export default class ContributorInfoEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  updateRcsId(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      contributorInformation: R.merge(this.props.tag.contributorInformation, {rcsId: e.target.value})
    }));
  }

  updateFirstName(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      contributorInformation: R.merge(this.props.tag.contributorInformation, { firstName: e.target.value })
    }));
  }

  updateLastName(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      contributorInformation: R.merge(this.props.tag.contributorInformation, { lastName: e.target.value })
    }));
  }

  updateTwitterHandle(e) {

    const fieldValue = e.target.value;
    const twitterHandle = fieldValue[0] === '@' ? fieldValue.substr(1, fieldValue.length) : fieldValue; //Strip out @

    this.props.updateTag(R.merge(this.props.tag, {
      contributorInformation: R.merge(this.props.tag.contributorInformation, {twitterHandle: twitterHandle})
    }));
  }

  updateContactEmail(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      contributorInformation: R.merge(this.props.tag.contributorInformation, {contactEmail: e.target.value})
    }));
  }

  updateBylineImage(image) {
    this.props.updateTag(R.merge(this.props.tag, {
      contributorInformation: R.merge(this.props.tag.contributorInformation, {bylineImage: image})
    }));
  }

  updateLargeBylineImage(image) {

    this.props.updateTag(R.merge(this.props.tag, {
      contributorInformation: R.merge(this.props.tag.contributorInformation, {largeBylineImage: image})
    }));
  }

  render () {

    const contributorInfomation = this.props.tag.contributorInformation || {};

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Contributor Information</label>
        <span className="tag-edit__fields">
          <div className="tag-edit__input-block">
            <label className="tag-edit__label-inline">First name</label>
            <input type="text"
              className="tag-edit__input"
              value={contributorInfomation.firstName || ''}
              onChange={this.updateFirstName.bind(this)}
              disabled={!this.props.tagEditable} />
          </div>
          <div className="tag-edit__input-block">
            <label className="tag-edit__label-inline">Last name
             <i className="i-info-grey"
                data-tip="Although the only mandatory field is 'Last name',<br/> always fill both fields when possible.<br/> If a single name/organisation/identifier is provided,<br/> only fill 'Last name'">
             </i>
            </label>
            <input type="text"
              className="tag-edit__input"
              value={contributorInfomation.lastName || ''}
              onChange={this.updateLastName.bind(this)}
              disabled={!this.props.tagEditable} />
          </div>
        </span>
        <div className="tag-edit__field">
          <label className="tag-edit__label">RCS ID</label>
          <input type="text"
            className="tag-edit__input"
            value={contributorInfomation.rcsId || ''}
            onChange={this.updateRcsId.bind(this)}
            disabled={!this.props.tagEditable}/>
        </div>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Twitter ID</label>
          <input type="text"
            className="tag-edit__input"
            value={contributorInfomation.twitterHandle ? '@' + contributorInfomation.twitterHandle : ''}
            onChange={this.updateTwitterHandle.bind(this)}
            disabled={!this.props.tagEditable}/>
        </div>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Contact Email</label>
          <input type="text"
            className="tag-edit__input"
            value={contributorInfomation.contactEmail || ''}
            onChange={this.updateContactEmail.bind(this)}
            disabled={!this.props.tagEditable}/>
        </div>
        <TagImageEdit
          tagImage={contributorInfomation.bylineImage}
          label="Byline Image"
          onChange={this.updateBylineImage.bind(this)}
          tagEditable={this.props.tagEditable}/>
        <TagImageEdit
          pngOnly={true}
          tagImage={contributorInfomation.largeBylineImage}
          label="Large Byline Image"
          onChange={this.updateLargeBylineImage.bind(this)}
          tagEditable={this.props.tagEditable}/>
        <ReactTooltip multiline={true} />
      </div>
    );
  }
}
