import React from 'react';
import SponsorLogo from './SponsorLogo.react';

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
          <label className="tag-edit__label">Name</label>
          <input type="text" value={this.props.sponsorship.sponsorName} onChange={this.updateName.bind(this)}/>
        </div>

        <div className="tag-edit__field">
          <label className="tag-edit__label">Logo</label>
          <SponsorLogo logo={this.props.sponsorship.sponsorLogo} onImageUpdated={this.updateLogo.bind(this)}/>
        </div>

        <div className="tag-edit__field">
          <label className="tag-edit__label">Link</label>
          <input type="text" value={this.props.sponsorship.sponsorLink} onChange={this.updateLink.bind(this)}/>
        </div>
      </div>
    );

  }
}
