import React from 'react';
import moment from 'moment';
import { Link } from 'react-router-dom';

export default class ActiveSponsorshipSummary extends React.Component {

  constructor(props) {
    super(props);
  }

  renderValidFrom(sponsorship) {
    if(sponsorship.validFrom) {
      return moment(sponsorship.validFrom).format('DD/MM/YYYY HH:mm');
    } else {
      return 'creation';
    }
  }

  renderValidTo(sponsorship) {
    if(sponsorship.validTo) {
      return moment(sponsorship.validTo).format('DD/MM/YYYY HH:mm');
    } else {
      return 'always';
    }
  }

  renderEditionTargeting(sponsorship) {
    if(sponsorship.targeting && sponsorship.targeting.validEditions) {
      return sponsorship.targeting.validEditions.join(', ');
    } else {
      return 'All';
    }
  }

  renderSponsorship(sponsorship) {
    return (
      <tr key={sponsorship.id} >
        <td><Link to={`/sponsorship/${sponsorship.id}`}>{sponsorship.sponsorName}<br/><img src={sponsorship.sponsorLogo.assets[0].imageUrl} /></Link></td>
        <td>{this.renderEditionTargeting(sponsorship)}</td>
        <td>
          Start: {this.renderValidFrom(sponsorship)}<br />
          End: {this.renderValidTo(sponsorship)}
        </td>
      </tr>
    );
  }

  renderSponsorships(sponsorships) {
    if(sponsorships.length == 0) {
      return (<span>No sponsorships found</span>);
    }

    return(<table className="grid-table tag-references">
      <thead className="tag-references__header">
      <tr>
        <th>
          Sponsor
        </th>
        <th>
          Editions
        </th>
        <th>
          Validity
        </th>
      </tr>
      </thead>
      <tbody className="tag-references__references">
      {sponsorships.map( s => this.renderSponsorship(s))}
      </tbody>
    </table>);
  }

  render () {
    return (
      <div className="tag-context__item">
        <div className="tag-context__header">Active sponsorships</div>
        {this.renderSponsorships(this.props.sponsorships)}
      </div>
    );
  }
}