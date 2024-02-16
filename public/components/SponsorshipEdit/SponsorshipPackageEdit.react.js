import React from 'react';

export default class SponsorshipPackageEdit extends React.Component {

    constructor(props) {
        super(props);
    }

    updateType(e) {
        this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
            sponsorshipPackage: e.target.value
        }));
    }


    render () {

        if (!this.props.sponsorship) {
            return false;
        }

        return (
            <div className="tag-edit__field">
                <label className="tag-edit__label">Package</label>
                <select value={this.props.sponsorship.sponsorshipPackage || "default"} onChange={this.updateType.bind(this)}>
                    <option value="default">default</option>
                    <option value="us">Advertising partner (US only)</option>
                    <option value="us-exclusive">Exclusive advertising partner (US only)</option>
                </select>
            </div>
        );
    }
}
