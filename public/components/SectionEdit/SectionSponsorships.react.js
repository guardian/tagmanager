import React from 'react';
import tagManagerApi from '../../util/tagManagerApi.js';
import ActiveSponsorshipSummary from '../utils/ActiveSponsorshipSummary.js';

export default class SectionSponsorships extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      sponsorships: []
    };
  }

  componentDidMount() {
    this.fetchSponsorshipsForSection();
  }

  fetchSponsorshipsForSection() {
    if (this.props.section ) {
      tagManagerApi.getActiveSponsorhipsForSection(this.props.section.id).then(res =>
        this.setState({sponsorships: res})
      );
    }
  }

  render () {
    return (
      <ActiveSponsorshipSummary sponsorships={this.state.sponsorships} />
    );
  }
}