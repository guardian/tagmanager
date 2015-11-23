import React from 'react';
import moment from 'moment';

import TagSelect from '../utils/TagSelect';

const CAPI_DATE_FORMAT = 'YYYY-MM-DD';

const PREDEFINED_VALUES = {
  today: {
    'from-date': moment().format(CAPI_DATE_FORMAT),
    'to-date': moment().format(CAPI_DATE_FORMAT)
  },
  thisweek: {
    'from-date': moment().subtract(6, 'days').format(CAPI_DATE_FORMAT),
    'to-date': moment().format(CAPI_DATE_FORMAT)
  },
  thismonth: {
    'from-date': moment().subtract(30, 'days').format(CAPI_DATE_FORMAT),
    'to-date': moment().format(CAPI_DATE_FORMAT)
  }
};

export default class BatchFilters extends React.Component {

    constructor(props) {
        super(props);
    }

    setFiltersToToday() {
      this.props.updateFilters(Object.assign({}, this.props.filters, PREDEFINED_VALUES.today));
    }

    isTodayActive() {
      return this.props.filters['to-date'] === PREDEFINED_VALUES.today['to-date'] &&
              this.props.filters['from-date'] === PREDEFINED_VALUES.today['from-date'];
    }

    setFiltersToThisWeek() {
      this.props.updateFilters(Object.assign({}, this.props.filters, PREDEFINED_VALUES.thisweek));
    }

    isThisWeekActive() {
      return this.props.filters['to-date'] === PREDEFINED_VALUES.thisweek['to-date'] &&
              this.props.filters['from-date'] === PREDEFINED_VALUES.thisweek['from-date'];
    }

    setFiltersToThisMonth() {
      this.props.updateFilters(Object.assign({}, this.props.filters, PREDEFINED_VALUES.thismonth));
    }

    isThisMonthActive() {
      return this.props.filters['to-date'] === PREDEFINED_VALUES.thismonth['to-date'] &&
              this.props.filters['from-date'] === PREDEFINED_VALUES.thismonth['from-date'];

    }

    clearDateFilter(e) {
      e.stopPropagation();

      this.props.updateFilters(Object.assign({}, this.props.filters, {
        'from-date': false,
        'to-date': false
      }));
    }

    removeTag(tag) {
      this.props.updateFilters(Object.assign({}, this.props.filters, {
        'tag': this.getTagArray().filter((t) => t !== tag).join(',')
      }));
    }

    addTag(tag) {
      this.props.updateFilters(Object.assign({}, this.props.filters, {
        'tag': this.getTagArray().concat([tag.path]).join(',')
      }));
    }

    getTagArray() {
      return this.props.filters.tag ? this.props.filters.tag.split(',') : [];
    }

    render () {

        return (
            <div className="batch-filters">
              <div className="batch-filters__filter">
                <div className="batch-filters__header">
                  Date
                </div>
                <div className={this.isTodayActive() ? 'batch-filters__option--active' : 'batch-filters__option'} onClick={this.setFiltersToToday.bind(this)}>
                  Today
                  <div className="batch-filters__option__clear" onClick={this.clearDateFilter.bind(this)}>
                    <i className="i-cross"></i>
                  </div>
                </div>
                <div className={this.isThisWeekActive() ? 'batch-filters__option--active' : 'batch-filters__option'} onClick={this.setFiltersToThisWeek.bind(this)}>
                  Last 7 Days
                  <div className="batch-filters__option__clear" onClick={this.clearDateFilter.bind(this)}>
                    <i className="i-cross"></i>
                  </div>
                </div>
                <div className={this.isThisMonthActive() ? 'batch-filters__option--active' : 'batch-filters__option'} onClick={this.setFiltersToThisMonth.bind(this)}>
                  Last 31 Days
                  <div className="batch-filters__option__clear" onClick={this.clearDateFilter.bind(this)}>
                    <i className="i-cross"></i>
                  </div>
                </div>
              </div>
              <div className="batch-filters__filter">
                <div className="batch-filters__header">
                  Has Tag
                </div>
                <div className="batch-filters__taglist">
                  {this.getTagArray().map(tag => {
                    return (
                      <div className='batch-filters__option--active'>
                        {tag}
                        <div className="batch-filters__option__clear" onClick={this.removeTag.bind(this, tag)}>
                          <i className="i-cross"></i>
                        </div>
                      </div>
                    );
                  })}
                  <TagSelect onTagClick={this.addTag.bind(this)} />
                </div>
              </div>
            </div>
        );
    }
}
