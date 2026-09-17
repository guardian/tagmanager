import React from 'react';

import {commercialTagTypes} from '../../../../constants/commercialTagTypes';

export default class CommercialInformation extends React.Component {

    constructor(props) {
        super(props);
    }

    updateCommercialType(e) {
        this.props.updateTag(Object.assign({}, this.props.tag, {
            commercialInformation: Object.assign({}, this.props.tag.commercialInformation, {
                commercialType: e.target.value
            })
        }));
    }

    render () {

        const selectCommercialType = this.props.tag.commercialInformation ? this.props.tag.commercialInformation.commercialType : undefined;

        return (
            <div className="tag-edit__input-group">
                <label className="tag-edit__input-group__header">Commercial Information</label>
                <div className="tag-edit__field">
                    <label className="tag-edit__label">Commercial Type</label>
                    <select value={selectCommercialType || ""} onChange={this.updateCommercialType.bind(this)} disabled={!this.props.tagEditable}>
                        {!selectCommercialType ? <option value={false}></option> : false}
                        {commercialTagTypes.sort((a, b) => {return a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1;}).map(function(t) {
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