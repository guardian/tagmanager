import React from 'react';
import TagImageEdit from '../TagEdit/formcomponents/TagImageEdit.react';

export default class SponsorEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  updateName(e) {
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      sponsorName: e.target.value
    }));
  }

  updateLogo(image) {
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      sponsorLogo: image
    }));
    //this.props.updateSponsorship(R.merge(this.props.tag, {
    //  contributorInformation: R.merge(this.props.tag.contributorInformation, {bylineImage: image})
    //}));
  }

  updateLink(e) {
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      sponsorLink: e.target.value
    }));
  }

  render () {

    if (!this.props.sponsorship) {
      return false;
    }

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Sponsor</label>
        <div className="tag-edit__field">
          <input type="text" value={this.props.sponsorship.sponsorName} onChange={this.updateName.bind(this)}/>
          <label className="tag-edit__label">Name</label>
        </div>

        <TagImageEdit
          tagImage={this.props.sponsorship.sponsorLogo}
          label="Logo"
          onChange={this.updateLogo.bind(this)}
          tagEditable={true}/>

        <div className="tag-edit__field">
          <input type="text" value={this.props.sponsorship.sponsorLink} onChange={this.updateLink.bind(this)}/>
          <label className="tag-edit__label">Link</label>
        </div>
      </div>
    );
  }
}
/*
 id: Long,
 validFrom: Option[DateTime],
 validTo: Option[DateTime],
 status: String,
 sponsorshipType: String,
 sponsorName: String,
 sponsorLogo: String,
 sponsorLink: String,
 tag: Option[Long],
 section: Option[Long],
 targetting: Option[SponsorshipTargeting])
 */
