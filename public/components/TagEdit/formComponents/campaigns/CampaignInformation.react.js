import React from 'react';

import {campaignTagTypes} from '../../../../constants/campaignTagTypes';

export default class CampaignInformation extends React.Component {

    constructor(props) {
        super(props);
    }

    updateCampaignType(e) {
        this.props.updateTag(Object.assign({}, this.props.tag, {
            campaignInformation: Object.assign({}, this.props.tag.campaignInformation, {
                campaignType: e.target.value
            })
        }));
    }

    render () {

        const selectCampaignType = this.props.tag.campaignInformation ? this.props.tag.campaignInformation.campaignType : undefined;

        return (
            <div className="tag-edit__input-group">
                <label className="tag-edit__input-group__header">Campaign Information</label>
                <div className="tag-edit__field">
                    <label className="tag-edit__label">Campaign Type</label>
                    <select value={selectCampaignType || ""} onChange={this.updateCampaignType.bind(this)} disabled={!this.props.tagEditable}>
                        {!selectCampaignType ? <option value={false}></option> : false}
                        {campaignTagTypes.sort((a, b) => {return a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1;}).map(function(t) {
                            return (
                                <option value={t.value} key={t.value} >{t.name}</option>
                            );
                        })}
                    </select>
                </div>
            </div>
        );
    }
}