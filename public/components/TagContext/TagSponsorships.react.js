import React from 'react';
import tagManagerApi from '../../util/tagManagerApi.js';
import ActiveSponsorshipSummary from '../utils/ActiveSponsorshipSummary.js';

export default class TagSponsorships extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      sponsorships: []
    };
  }

  componentDidMount() {
    this.fetchSponsorshipsForTag();
  }
 
  fetchSponsorshipsForTag() {
    if (this.showSponsorshipComponent()) {
      tagManagerApi.getActiveSponsorhipsForTag(this.props.tag.id).then(res =>
        this.setState({sponsorships: res})
      );
    }
  }

  showSponsorshipComponent () {
    return this.props.tag &&
      (this.props.tag.type === 'Topic' || this.props.tag.type === 'Series');
  }

  render () {
    if (!this.showSponsorshipComponent()) {
      return false;
    }

    return (
      <ActiveSponsorshipSummary sponsorships={this.state.sponsorships} />
    );
  }
}