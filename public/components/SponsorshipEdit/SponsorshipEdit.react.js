import React from 'react';

export default class SponsorshipEdit extends React.Component {

    constructor(props) {
      super(props);
    }

    render () {

      if (!this.props.sponsorship) {
        return false;
      }

      return (
        <h2>Hello from the sponsorship edit form</h2>
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
