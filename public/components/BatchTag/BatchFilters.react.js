import React from 'react';
import moment from 'moment';
import R from 'ramda';

import TagSelect from '../utils/TagSelect';
import SectionSelect from '../utils/SectionSelect.react';

import DateTimePicker from 'react-widgets/lib/DateTimePicker';
import momentLocalizer from 'react-widgets/lib/localizers/moment';
momentLocalizer(moment);

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

    setFromDate(date, string) {
      this.props.updateFilters(Object.assign({}, this.props.filters, {
        'from-date': moment(string, 'DD/MM/YYYY').format(CAPI_DATE_FORMAT)
      }));
    }

    setToDate(date) {
      this.props.updateFilters(Object.assign({}, this.props.filters, {
        'to-date': moment(date).format(CAPI_DATE_FORMAT)
      }));

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
      const tagPath = tag.type === "ContentType" ? "type/" + tag.slug : tag.path;
      this.props.updateFilters(Object.assign({}, this.props.filters, {
        'tag': R.uniq(this.getTagArray().concat([tagPath])).join(',')
      }));
    }

    getTagArray() {
      return this.props.filters.tag ? this.props.filters.tag.split(',') : [];
    }

    updateSectionFilter(e) {

      const section = this.props.sections.filter((section) => section.id === parseInt(e.target.value, 10));

      if (section.length) {
        this.props.updateFilters(Object.assign({}, this.props.filters, {
          'section': section[0].path
        }));
      } else {
        this.props.updateFilters(R.omit('section', this.props.filters));
      }
    }

    changeDateBasis(e) {
      this.props.updateFilters(Object.assign({}, this.props.filters, {
        'use-date': e.target.value
      }));
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

                <div className="batch-filters__custom-date">
                  <span className="batch-filters__label">From:</span>
                  <DateTimePicker
                    format={"DD/MM/YYYY"}
                    time={false}
                    value={ this.props.filters['from-date'] ? new Date(this.props.filters['from-date']) : null}
                    onChange={this.setFromDate.bind(this)}/>
                  <span className="batch-filters__label">To:</span>
                    <DateTimePicker
                      format={"DD/MM/YYYY"}
                      time={false}
                      value={ this.props.filters['to-date'] ? new Date(this.props.filters['to-date']) : null}
                      onChange={this.setToDate.bind(this)}/>
                </div>
                  <select onChange={this.changeDateBasis.bind(this)}>
                    <option value="published">Published Date</option>
                    <option value="last-modified">Last Modified</option>
                  </select>
              </div>
              <div className="batch-filters__filter">
                <div className="batch-filters__header">
                  Has Tag
                </div>
                <div className="batch-filters__taglist">
                  {this.getTagArray().map(tag => {
                    return (
                      <div className='batch-filters__option--active' key={tag}>
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
              <div className="batch-filters__filter">
                <div className="batch-filters__header">
                  In Section
                </div>
                <div className="batch-filters__taglist">
                  <SectionSelect
                    showBlank={true}
                    onChange={this.updateSectionFilter.bind(this)}
                    sections={this.props.sections}
                    selectedId={this.props.filters.section ? this.props.sections.filter((section) => section.path === this.props.filters.section)[0].id : undefined} />
                </div>
              </div>
            </div>
        );
    }
}
