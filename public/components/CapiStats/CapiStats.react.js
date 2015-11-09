import React from 'react';
import moment from 'moment';
import capiApi from '../../util/capi.js';

export default class SaveButton extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          dayUses: undefined,
          prevDayUses: undefined,
          weekUses: undefined,
          prevWeekUses: undefined,
          totalUses: undefined
        };

        this.fetchUseStats = this.fetchUseStats.bind(this);
    }

    componentDidMount() {
      this.fetchUseStats();
    }

    fetchUseStats() {

      const CAPI_DATE_FORMAT = 'YYYY-MM-DDTHH:MM:SS';
      const today = moment().subtract(1, 'days').format(CAPI_DATE_FORMAT);
      const yesterday = moment().subtract(2, 'days').format(CAPI_DATE_FORMAT);
      const thisweek = moment().subtract(7, 'days').format(CAPI_DATE_FORMAT);
      const lastweek = moment().subtract(14, 'days').format(CAPI_DATE_FORMAT);

      capiApi.getByTag(this.props.tag, today)
        .then(res => {
          this.setState({
            dayUses: res.response.total
          });
        });

      capiApi.getByTag(this.props.tag, thisweek)
        .then(res => {
          this.setState({
            weekUses: res.response.total
          });
        });
      capiApi.getByTag(this.props.tag, yesterday, today)
        .then(res => {
          this.setState({
            prevDayUses: res.response.total
          });
        });

      capiApi.getByTag(this.props.tag, lastweek, thisweek)
        .then(res => {
          this.setState({
            prevWeekUses: res.response.total
          });
        });

      capiApi.getByTag(this.props.tag)
        .then(res => {
          this.setState({
            totalUses: res.response.total
          });
        });
    }

    render () {
      return (
        <div className="capi-stats">
          <div className="capi-stats__header">CAPI Stats</div>
          <div className="capi-stats__stat">
            <div className="capi-stats__stat__header">Today</div>
            <div className="capi-stats__stat__value">{this.state.dayUses !== undefined ? this.state.dayUses : '...'}</div>
            <div className="capi-stats__stat__prev">(Yesterday {this.state.prevDayUses !== undefined ? this.state.prevDayUses : '...'}) </div>
          </div>
          <div className="capi-stats__stat">
            <div className="capi-stats__stat__header">This week</div>
            <div className="capi-stats__stat__value">{this.state.weekUses !== undefined ? this.state.weekUses : '...'}</div>
            <div className="capi-stats__stat__prev">(Last week {this.state.prevWeekUses !== undefined ? this.state.prevWeekUses : '...'}) </div>
          </div>
          <div className="capi-stats__stat">
            <div className="capi-stats__stat__header">Total</div>
            <div className="capi-stats__stat__value">{this.state.totalUses !== undefined ? this.state.totalUses : '...'}</div>
          </div>
        </div>
      );
    }
}
