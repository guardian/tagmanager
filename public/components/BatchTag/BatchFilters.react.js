import React from 'react';
import moment from 'moment';

const CAPI_DATE_FORMAT = 'YYYY-MM-DD';

export default class BatchFilters extends React.Component {

    constructor(props) {
        super(props);
    }

    setFiltersToToday() {
      this.props.updateFilters(Object.assign({}, this.props.filters, {
        'from-date': moment().format(CAPI_DATE_FORMAT),
        'to-date': moment().format(CAPI_DATE_FORMAT)
      }));
    }

    setFiltersToThisWeek() {
      this.props.updateFilters(Object.assign({}, this.props.filters, {
        'from-date': moment().subtract(6, 'days').format(CAPI_DATE_FORMAT),
        'to-date': moment().format(CAPI_DATE_FORMAT)
      }));
    }

    setFiltersToThisMonth() {
      this.props.updateFilters(Object.assign({}, this.props.filters, {
        'from-date': moment().subtract(30, 'days').format(CAPI_DATE_FORMAT),
        'to-date': moment().format(CAPI_DATE_FORMAT)
      }));
    }

    clearDateFilter() {
      this.props.updateFilters(Object.assign({}, this.props.filters, {
        'from-date': false,
        'to-date': false
      }));
    }

    render () {
        return (
            <div className="batch-filters">
              <div className="batch-filters__filter">
                <div className="batch-filters__header">
                  Date
                </div>
                <div className="batch-filters__option" onClick={this.setFiltersToToday.bind(this)}>
                  Today
                </div>
                <div className="batch-filters__option" onClick={this.setFiltersToThisWeek.bind(this)}>
                  Last 7 Days
                </div>
                <div className="batch-filters__option" onClick={this.setFiltersToThisMonth.bind(this)}>
                  Last 31 Days
                </div>
                <div className="batch-filters__option" onClick={this.clearDateFilter.bind(this)}>
                  Clear
                </div>
              </div>
            </div>
        );
    }
}
