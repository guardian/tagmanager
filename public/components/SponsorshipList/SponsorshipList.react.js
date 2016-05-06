import React from 'react';
import history from '../../routes/history';
import {Link} from 'react-router';
import moment from 'moment';

export default class SponsorshipList extends React.Component {

    constructor(props) {
        super(props);
    }

    generateSponsorshipUrl(sponsorship) {
      if (sponsorship.sponsorshipType === 'paidContent' && sponsorship.tags && sponsorship.tags.length > 0) {
        return '/tag/' + sponsorship.tags[0].id;
      } else {
        return '/sponsorship/' + sponsorship.id;
      }
    }

    onSponsorshipClick(sponsorship) {
      history.replaceState(null, this.generateSponsorshipUrl(sponsorship));
    }

    renderTargeting(sponsorship) {
      const tags = sponsorship.tags && sponsorship.tags.length && (
              <span>Tags: {sponsorship.tags.map(function(t){return t.internalName}).join(', ')}</span>
          );
      const sections = sponsorship.sections && sponsorship.sections.length && (
              <span>Section: {sponsorship.sections.map(function(s){return s.name}).join(', ')}</span>
          );
      const untargeted = !tags && !sections && (<span>Untargeted</span>);

        return (<span>
            {tags}
            {!!tags && !!sections && <br/>}
            {sections}
            {untargeted}
        </span>);
    }

    renderValidFrom(sponsorship) {
      if (sponsorship.validFrom) {
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

      const sponsorshipClickHandler = (e) => {
        e.preventDefault();
        this.onSponsorshipClick(sponsorship);
      };

      return (
        <a key={sponsorship.id}  href={this.generateSponsorshipUrl(sponsorship)} onClick={sponsorshipClickHandler}>
          <div className="sponsorshiplist__row" onClick={this.onSponsorshipClick.bind(this, sponsorship)}>
            <div className="sponsorshiplist__sponsortype">{sponsorship.sponsorshipType} </div>
            <div className="sponsorshiplist__sponsorname">
              <img className="sponsorshiplist__sponsorname__image" src={sponsorship.sponsorLogo.assets[0].imageUrl} />
              {sponsorship.sponsorName}
            </div>
            <div className="sponsorshiplist__sponsortarget">{this.renderTargeting(sponsorship)}</div>
            <div className="sponsorshiplist__sponsorfrom">{this.renderValidFrom(sponsorship)}</div>
            <div className="sponsorshiplist__sponsorto">{this.renderValidTo(sponsorship)}</div>
            <div className="sponsorshiplist__sponsorstatus">{sponsorship.status}</div>
          </div>
        </a>
      );
    }

    render () {

      if (!this.props.sponsorships || !this.props.sponsorships.length) {
        return (
          <div>Fetching sponsorships...</div>
        );
      }

      return (
        <div className="sponsorshiplist">
          <div className="sponsorshiplist__row">
              <div className="sponsorshiplist__sponsortype--header" onClick={this.props.sortBy.bind(this, 'sponsorshipType')}>Type</div>
              <div className="sponsorshiplist__sponsorname--header" onClick={this.props.sortBy.bind(this, 'sponsor')}>Sponsor</div>
              <div className="sponsorshiplist__sponsortarget--header">Target</div>
              <div className="sponsorshiplist__sponsorfrom--header" onClick={this.props.sortBy.bind(this, 'from')}>Active from</div>
              <div className="sponsorshiplist__sponsorto--header" onClick={this.props.sortBy.bind(this, 'to')}>Active to</div>
              <div className="sponsorshiplist__sponsorstatus--header" onClick={this.props.sortBy.bind(this, 'status')}>Status</div>
          </div>
          {this.props.sponsorships.map(this.renderListItem.bind(this))}
        </div>
      );
    }
}
