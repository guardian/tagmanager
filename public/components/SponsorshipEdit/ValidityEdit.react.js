import React from 'react';
import moment from 'moment';

import DateTimePicker from 'react-widgets/lib/DateTimePicker';
import momentLocalizer from 'react-widgets/lib/localizers/moment';

momentLocalizer(moment);

export default class ValidityEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  setValidFrom(date) {
    console.log(date, moment(date));
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      validFrom: moment(date)
    }));
  }

  setValidTo(date) {
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      validTo: moment(date)
    }));
  }

  render () {

    if (!this.props.sponsorship) {
      return false;
    }

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Validity</label>

        <div className="tag-edit__field" >
          <label className="tag-edit__input-group__header">From</label>
          <DateTimePicker
            format={"DD/MM/YYYY HH:mm"}
            value={ this.props.sponsorship.validFrom ? new Date(this.props.sponsorship.validFrom) : null}
            onChange={this.setValidFrom.bind(this)}/>
        </div>

        <div className="tag-edit__field" >
          <label className="tag-edit__input-group__header">To</label>
          <DateTimePicker
            format={"DD/MM/YYYY HH:mm"}
            value={ this.props.sponsorship.validTo ? new Date(this.props.sponsorship.validTo) : null}
            onChange={this.setValidTo.bind(this)}/>
        </div>
      </div>
    );
  }
}
