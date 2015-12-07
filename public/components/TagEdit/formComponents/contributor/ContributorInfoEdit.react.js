
import React from 'react';
import R from 'ramda';

import TagImageEdit from '../TagImageEdit.react';

export default class ContributorInfoEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  updateRcsId(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      rcsId: e.target.value
    }));
  }

  updateTwitterHandle(e) {

    const fieldValue = e.target.value;
    const twitterHandle = fieldValue[0] === '@' ? fieldValue.substr(1, fieldValue.length) : fieldValue; //Strip out @

    this.props.updateTag(R.merge(this.props.tag, {
      twitterHandle: twitterHandle
    }));
  }

  updateContactEmail(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      contactEmail: e.target.value
    }));
  }

  updateBylineImage(image) {
    this.props.updateTag(R.merge(this.props.tag, {
      bylineImage: image
    }));
  }

  updateLargeBylineImage(image) {
    this.props.updateTag(R.merge(this.props.tag, {
      largeBylineImage: image
    }));
  }

  render () {

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Contributor Information</label>
        <div className="tag-edit__field">
          <label className="tag-edit__label">RCS ID</label>
          <input type="text" className="tag-edit__input" value={this.props.tag.rcsId || ''} onChange={this.updateRcsId.bind(this)}/>
        </div>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Twitter ID</label>
          <input type="text" className="tag-edit__input" value={this.props.tag.twitterHandle ? '@' + this.props.tag.twitterHandle : ''} onChange={this.updateTwitterHandle.bind(this)}/>
        </div>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Contact Email</label>
          <input type="text" className="tag-edit__input" value={this.props.tag.contactEmail || ''} onChange={this.updateContactEmail.bind(this)}/>
        </div>
        <TagImageEdit tagImage={this.props.tag.bylineImage} label="Byline Image" onChange={this.updateBylineImage.bind(this)}/>
        <TagImageEdit tagImage={this.props.tag.largeBylineImage} label="Large Byline Image" onChange={this.updateLargeBylineImage.bind(this)}/>
      </div>
    );
  }
}
