import React from 'react';
import moment from 'moment';

export default class ClashWarning extends React.Component {

  constructor(props) {
    super(props);
  }

  renderTargeting(sponsorship) {
    if(sponsorship.tag) {
      return (<p>Tag: {sponsorship.tag.internalName}</p>);
    } else if(sponsorship.section) {
      return (<p>Section: {sponsorship.section.name}</p>);
    } else {
      return (<p>Untargeted</p>);
    }
  }

  renderValidFrom(sponsorship) {
    if(sponsorship.validFrom) {
      return moment(sponsorship.validFrom).format('DD/MM/YYYY HH:mm:ss');
    } else {
      return 'creation'
    }
  }

  renderValidTo(sponsorship) {
    if(sponsorship.validTo) {
      return moment(sponsorship.validTo).format('DD/MM/YYYY HH:mm:ss');
    } else {
      return 'always'
    }
  }

  renderListItem(sponsorship) {

    return (
      <tr key={sponsorship.id} className="taglist__results-item" >
        <td><img src={sponsorship.sponsorLogo.assets[0].imageUrl} />{sponsorship.sponsorName} </td>
        <td>{this.renderTargeting(sponsorship)}</td>
        <td>{this.renderValidFrom(sponsorship)}</td>
        <td>{this.renderValidTo(sponsorship)}</td>
      </tr>
    );
  }

  render() {
    if (!this.props.clashingSponsorships || !this.props.clashingSponsorships.length || this.props.clashingSponsorships.length === 0) {
      return false;
    }

    return (
      <div className="tag-audit">
        <div className="tag-audit__header sponsorship-validation">Warning: this sponsorship clashes with other sponsorships</div>
        <table className="sponsorship-clash">
          <thead className="taglist__header">
          <tr>
            <th>Sponsor</th>
            <th>Target</th>
            <th>Active from</th>
            <th>Active to</th>
          </tr>
        </thead>
        <tbody className="taglist__results">
        {this.props.clashingSponsorships.map(this.renderListItem.bind(this))}
        </tbody>
        </table>
      </div>
    );
  }
}
