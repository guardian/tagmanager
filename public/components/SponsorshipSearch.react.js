import React from 'react';
import { Link } from 'react-router';
import tagManagerApi from '../util/tagManagerApi';
import SponsorshipList from './SponsorshipList/SponsorshipList.react';

const searchStatuses = {
  'All': 'all',
  'Active': 'active',
  'Pending': 'pending',
  'Expiring soon': 'expiringSoon',
  'Expired recently': 'expiredRecently',
  'Launching soon': 'launchingSoon',
  'Launched recently': 'launchedRecently',
  'Expired': 'expired'
};

const sponsorshipTypes = {
  'All': 'all',
  'Sponsorship': 'sponsored',
  'Foundation': 'foundation',
  'Paid content': 'paidContent',
}

export class SponsorshipSearch extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          searchString: '',
          sponsorships: [],
          sortResultsBy: 'sponsorName',
          status: 'all',
          type: 'all'
        };

        this.sortBy = this.sortBy.bind(this);
        this.searchSponsorships = this.searchSponsorships.bind(this);
    }

    componentDidMount() {
      this.searchSponsorships('', undefined, 'all', 'all');
    }

    searchSponsorships(searchString, sortBy, status, type) {

      var self = this;

      this.setState({
        searchString: searchString !== undefined ? searchString : this.state.searchString,
        sortBy: sortBy !== undefined ? sortBy : this.state.sortBy,
        status: status !== undefined ? status : this.state.status,
        type: type !== undefined ? type : this.state.type,
      });

      tagManagerApi.searchSponsorships({
        searchString: searchString,
        sortBy: sortBy,
        status: status,
        type: type
      })
      .then(function(resp) {
          self.setState({sponsorships: resp});
      }).fail(function(err, msg) {
          console.log('failed', err, msg);
      });
    }

    handleSearchInputChange(event) {
      this.searchSponsorships(event.target.value, this.state.sortBy, this.state.status, this.state.type);
    }

    handleSearchStatusChange(event) {
      this.searchSponsorships(this.state.searchString, this.state.sortBy, event.target.value, this.state.type);
    }

    handleSearchTypeChange(event) {
      this.searchSponsorships(this.state.searchString, this.state.sortBy, this.state.status, event.target.value);
    }

    sortBy(fieldName) {
      this.searchSponsorships(this.state.searchString, fieldName, this.state.status, this.state.type);
    }

    render () {
        return (
            <div className="tag-search">
                <div className="tag-search__filters">
                    <div className="tag-search__filters__group">
                        <label>Search</label>
                        <input className="tag-search__input" type="text" value={this.state.searchString} onChange={this.handleSearchInputChange.bind(this)} />
                        <span>
                          Status:
                          <select onChange={this.handleSearchStatusChange.bind(this)} value={this.state.status}>
                            {Object.keys(searchStatuses).map((field) => {
                              return (<option key={field} value={searchStatuses[field]}>{field}</option>);
                            })}
                          </select>
                        </span>
                        <span>
                          Type:
                          <select onChange={this.handleSearchTypeChange.bind(this)} value={this.state.type}>
                            {Object.keys(sponsorshipTypes).map((field) => {
                              return (<option key={field} value={sponsorshipTypes[field]}>{field}</option>);
                            })}
                          </select>
                        </span>

                    </div>

                    <Link className="tag-search__create" to="/sponsorship/create">Create a new sponsorship</Link>

                </div>
                <div className="tag-search__suggestions">
                    <SponsorshipList sponsorships={this.state.sponsorships} sortBy={this.sortBy} />
                </div>
            </div>
        );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getSections from '../actions/SectionsActions/getSections';

function mapStateToProps(state) {
  return {
    sections: state.sections
  };
}

function mapDispatchToProps(dispatch) {
  return {
    sectionActions: bindActionCreators(Object.assign({}, getSections), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(SponsorshipSearch);
