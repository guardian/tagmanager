import React from 'react';
import SponsorLogo from './SponsorLogo.react';
import {PAID_HOSTEDCONTENT_TYPE} from '../../constants/paidContentTagTypes';
import ReactTooltip from 'react-tooltip';

const imageRules = `
  <p style="text-align:left; margin-left: -10px">This image should be:</p>
  <ul style="list-style-type: circle; margin-left: 5px;">
    <li>280 pixels wide</li>
    <li>180 pixels tall</i>
    <li>A .png or .jpg file</li>
  </ul>
`

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

  updateHighContrastLogo(image) {
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      highContrastSponsorLogo: image
    }));
  }

  updateLink(e) {
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      sponsorLink: e.target.value.trim()
    }));
  }

  updateAboutLink(e) {
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      aboutLink: e.target.value.trim()
    }));
  }

  render () {

    if (!this.props.sponsorship) {
      return false;
    }

    const logoWidth = this.props.paidContentTagType === PAID_HOSTEDCONTENT_TYPE.value ? false : 280;
    const logoHeight = this.props.paidContentTagType === PAID_HOSTEDCONTENT_TYPE.value ? false : 180;

    return (
      <div className="tag-edit__input-group">
        <ReactTooltip html={true}/>
        <label className="tag-edit__input-group__header">Sponsor</label>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Name</label>
          <input type="text" className="tag-edit__input" value={this.props.sponsorship.sponsorName || ""} onChange={this.updateName.bind(this)}/>
        </div>

        <div className="tag-edit__field">
          <label className="tag-edit__label">Logo</label>
          <span data-tip={imageRules}><i className="i-info-grey sponsor-tooltip" /></span>
          <SponsorLogo logo={this.props.sponsorship.sponsorLogo} onImageUpdated={this.updateLogo.bind(this)} requiredWidth={logoWidth} requiredHeight={logoHeight}/>
        </div>

        <div className="tag-edit__field">
          <label className="tag-edit__label">Link</label>
          <input type="text" className="tag-edit__input" value={this.props.sponsorship.sponsorLink || ""} onChange={this.updateLink.bind(this)}/>
        </div>

        <div className="tag-edit__field">
          <label className="tag-edit__label">About this content Link</label>
          <input type="text" className="tag-edit__input" value={this.props.sponsorship.aboutLink || ""} onChange={this.updateAboutLink.bind(this)}/>
        </div>

        <div className="tag-edit__field">
          <label className="tag-edit__label">High contrast logo</label>
          <div className="tag-edit__image__info">This optional logo will be used on media pages with a dark background</div>
          <SponsorLogo logo={this.props.sponsorship.highContrastSponsorLogo} onImageUpdated={this.updateHighContrastLogo.bind(this)} requiredWidth={logoWidth} requiredHeight={logoHeight}/>
        </div>

      </div>
    );

  }
}
