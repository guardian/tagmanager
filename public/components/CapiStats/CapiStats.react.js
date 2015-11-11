import React from 'react';
import moment from 'moment';
import CapiClient from '../../util/CapiClient.js';

export default class CapiStats extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          dayUses: undefined,
          prevDayUses: undefined,
          weekUses: undefined,
          prevWeekUses: undefined,
          totalUses: undefined
        };

        if (this.props.config.capiUrl && this.props.config.capiKey) {
          const capiClient = CapiClient(this.props.config.capiUrl, this.props.config.capiKey);
          this.fetchUseStats = this.fetchUseStats.bind(this, capiClient);
        } else {
          console.log('No CAPI Keys found in state.config');
        }

    }

    componentDidMount() {
      this.fetchUseStats();
    }

    fetchUseStats(capiClient) {

      if (!capiClient) {
        return;
      }

      const CAPI_DATE_FORMAT = 'YYYY-MM-DDTHH:mm:ss';
      const today = moment().subtract(1, 'days').format(CAPI_DATE_FORMAT);
      const yesterday = moment().subtract(2, 'days').format(CAPI_DATE_FORMAT);
      const thisweek = moment().subtract(7, 'days').format(CAPI_DATE_FORMAT);
      const lastweek = moment().subtract(14, 'days').format(CAPI_DATE_FORMAT);

      capiClient.getByTag(this.props.tag, {
        'from-date': today,
        'page-size': 0
      }).then(res => {
          this.setState({
            dayUses: res.response.total
          });
        });

      capiClient.getByTag(this.props.tag, {
        'from-date': thisweek,
        'page-size': 0
      }).then(res => {
          this.setState({
            weekUses: res.response.total
          });
        });

      capiClient.getByTag(this.props.tag, {
        'from-date': yesterday,
        'to-date': today,
        'page-size': 0
      }).then(res => {
          this.setState({
            prevDayUses: res.response.total
          });
        });

        capiClient.getByTag(this.props.tag, {
          'from-date': lastweek,
          'to-date': thisweek,
          'page-size': 0
        }).then(res => {
          this.setState({
            prevWeekUses: res.response.total
          });
        });

      capiClient.getByTag(this.props.tag, {
        'page-size': 0
      }).then(res => {
          this.setState({
            totalUses: res.response.total
          });
        });
    }

    render () {

      var dayclass = 'capi-stats__stat__arrow';
      if (this.state.dayUses > this.state.prevDayUses) {
        dayclass += '--increase';
      } else if (this.state.dayUses < this.state.prevDayUses) {
        dayclass += '--decrease';
      }

      var weekclass = 'capi-stats__stat__arrow';
      if (this.state.weekUses > this.state.prevWeekUses) {
        weekclass += '--increase';
      } else if (this.state.weekUses < this.state.prevWeekUses) {
        weekclass += '--decrease';
      }

      return (
        <div className="capi-stats">
          <div className="capi-stats__header">CAPI Stats</div>
          <div className="capi-stats__stat">
            <div className="capi-stats__stat__header">Last 24 Hours</div>
            <div className="capi-stats__stat__value">
              {this.state.dayUses !== undefined ? this.state.dayUses : '...'}
              <span className={dayclass} title={'Previous 24hrs: ' + this.state.prevDayUses}></span>
            </div>
          </div>
          <div className="capi-stats__stat">
            <div className="capi-stats__stat__header">This week</div>
            <div className="capi-stats__stat__value">
              {this.state.weekUses !== undefined ? this.state.weekUses : '...'}
              <span className={weekclass} title={'Previous Week: ' + this.state.prevWeekUses}></span>

            </div>
          </div>
          <div className="capi-stats__stat">
            <div className="capi-stats__stat__header">Total uses</div>
            <div className="capi-stats__stat__value">{this.state.totalUses !== undefined ? this.state.totalUses : '...'}</div>
          </div>
        </div>
      );
    }
}
