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
      <div className="tag-edit__field">
        { this.props.editable ? <>
            <label className="tag-edit__label">Type</label>
            <select value={this.props.sponsorship.sponsorshipType} onChange={this.updateType.bind(this)}>
              <option value="sponsored">sponsored</option>
              <option value="foundation">foundation</option>
            </select>
          </> : this.props.sponsorship.sponsorshipType }
      </div>
    );
  }
}
