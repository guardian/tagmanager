import React from 'react';

export default class SponsorshipTypeEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  updateType(e) {
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      sponsorshipType: e.target.value
    }));
  }


  render () {

    if (!this.props.sponsorship) {
      return false;
    }

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Sponsorship type</label>
        <div className="tag-edit__field">
          <select value={this.props.sponsorship.sponsorshipType} onChange={this.updateType.bind(this)}>
            <option value="sponsored">sponsored</option>
            <option value="foundation">foundation</option>
          </select>
        </div>
      </div>
    );
  }
}
