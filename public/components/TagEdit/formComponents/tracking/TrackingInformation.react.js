import React from 'react';

import {trackingTagTypes} from '../../../../constants/trackingTagTypes';

export default class TrackingInformation extends React.Component {

  constructor(props) {
    super(props);
  }

  updateTrackingType(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      trackingInformation: Object.assign({}, this.props.tag.trackingInformation, {
        trackingType: e.target.value
      })
    }));
  }

  render () {

    const selectTrackingType = this.props.tag.trackingInformation ? this.props.tag.trackingInformation.trackingType : undefined;

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Tracking Information</label>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Tracking Type</label>
          <select value={selectTrackingType} onChange={this.updateTrackingType.bind(this)} disabled={!this.props.tagEditable}>
            {!selectTrackingType ? <option value={false}></option> : false}
            {trackingTagTypes.sort((a, b) => {return a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1;}).map(function(t) {
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
