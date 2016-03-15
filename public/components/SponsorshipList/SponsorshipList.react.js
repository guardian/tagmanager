import React from 'react';
import history from '../../routes/history';
import {Link} from 'react-router';
import moment from 'moment';

export default class SponsorshipList extends React.Component {

    constructor(props) {
        super(props);
    }

    onSponsorshipClick(sponsorship) {
      if(sponsorship.sponsorshipType === 'paidContent'){
        history.replaceState(null, '/tag/' + sponsorship.tag.id);
      } else {
        history.replaceState(null, '/sponsorship/' + sponsorship.id);
      }
    }

    renderTargeting(sponsorship) {
      if(sponsorship.tag) {
        return (<div>Tag: {sponsorship.tag.internalName}</div>);
      } else if(sponsorship.section) {
        return (<div>Section: {sponsorship.section.name}</div>);
      } else {
        return (<div>Untargeted</div>);
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
        <tr key={sponsorship.id} className="taglist__results-item" onClick={this.onSponsorshipClick.bind(this, sponsorship)}>
          <td>{sponsorship.sponsorshipType} </td>
          <td><img src={sponsorship.sponsorLogo.assets[0].imageUrl} />{sponsorship.sponsorName} </td>
          <td>{this.renderTargeting(sponsorship)}</td>
          <td>{this.renderValidFrom(sponsorship)}</td>
          <td>{this.renderValidTo(sponsorship)}</td>
          <td>{sponsorship.status}</td>
        </tr>
      );
    }

    render () {

      if (!this.props.sponsorships || !this.props.sponsorships.length) {
        return (
          <div>Fetching sponsorships...</div>
        );
      }

      return (
        <table className="taglist">
          <thead className="taglist__header">
            <tr>
              <th onClick={this.props.sortBy.bind(this, 'sponsorshipType')}>Type</th>
              <th onClick={this.props.sortBy.bind(this, 'sponsor')}>Sponsor</th>
              <th>Target</th>
              <th onClick={this.props.sortBy.bind(this, 'from')}>Active from</th>
              <th onClick={this.props.sortBy.bind(this, 'to')}>Active to</th>
              <th onClick={this.props.sortBy.bind(this, 'status')}>Status</th>
            </tr>
          </thead>
          <tbody className="taglist__results">
          {this.props.sponsorships.map(this.renderListItem.bind(this))}
          </tbody>
        </table>

      );
    }
}
