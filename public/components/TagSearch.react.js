import React from 'react';
import { Link } from 'react-router';
import tagManagerApi from '../util/tagManagerApi';
import TagsList from './TagList/TagList.react';
import PageNavigator from './utils/PageNavigator.react';
import {hasPermission} from '../util/verifyPermission'
import ReactTooltip from 'react-tooltip'

const searchFields = {
  'Internal Name': 'internalName',
  'External Name': 'externalName',
  'ID': 'id',
  'Type': 'type',
  'Path': 'path'
};

const PAGE_NAV_SPAN = 5;

export class TagSearch extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          searchString: '',
          tags: [],
          sortResultsBy: 'internalName',
          searchFieldName:'internalName',
          currentPage: 1,
          tagCount: 0
        };

        this.sortBy = this.sortBy.bind(this);
        this.searchTags = this.searchTags.bind(this);

        if (!this.props.sections || !this.props.sections.length) {
          this.props.sectionActions.getSections();
        }
    }

    componentDidMount() {
      this.searchTags();
    }

    pageSelectCallback(page) {
      const self = this;
      this.setState({currentPage: page});

      tagManagerApi.searchTags(this.state.searchString, {
        searchFieldName: this.state.searchFieldName,
        orderByField: this.state.sortBy,
        page: page
      })
      .then(function(resp) {
          self.setState({tags: resp.tags});
      }).fail(function(err, msg) {
          console.log('failed', err, msg);
      });
    }

    searchTags(searchString, searchFieldName, sortBy) {

      var self = this;

      this.setState({
        searchString: searchString !== undefined ? searchString : this.state.searchString,
        sortBy: sortBy !== undefined ? sortBy : this.state.sortBy,
        searchFieldName: searchFieldName !== undefined ? searchFieldName : this.state.searchFieldName,
        currentPage: 1
      });

      tagManagerApi.searchTags(searchString, {
        searchFieldName: searchFieldName,
        orderByField: sortBy
      })
      .then(function(resp) {
        self.setState({
          tags: resp.tags,
          tagCount: resp.count
        });
      }).fail(function(err, msg) {
          console.log('failed', err, msg);
      });
    }

    handleSearchInputChange(event) {
      this.searchTags(event.target.value, this.state.searchFieldName, this.state.sortBy);
    }

    handleSearchFieldChange(event) {
      this.searchTags(this.state.searchString, event.target.value, this.state.sortBy);

    }

    sortBy(fieldName) {
      this.searchTags(this.state.searchString, this.state.searchFieldName, fieldName);
    }

    renderPageNavigator() {

      const count = this.state.tagCount;
      if (count > 0 && count > this.props.config.tagSearchPageSize) {
        return (
          <PageNavigator
            pageSelectCallback={this.pageSelectCallback.bind(this)}
            currentPage={this.state.currentPage}
            pageSpan={PAGE_NAV_SPAN}
            lastPage={Math.ceil(count / this.props.config.tagSearchPageSize)}
          />
        );
      }

      return false;
    }

    render () {
        const canCreateTags = hasPermission("tagEdit");

        return (
            <div className="tag-search">
                <ReactTooltip multiline={true} />
                <div className="tag-search__filters">
                    <div className="tag-search__filters__group">
                        <label>Search</label>
                        <input className="tag-search__input" type="text" value={this.state.searchString} onChange={this.handleSearchInputChange.bind(this)} />
                        <select onChange={this.handleSearchFieldChange.bind(this)} value={this.state.searchFieldName}>
                          {Object.keys(searchFields).map((field) => {
                            return (<option key={field} value={searchFields[field]}>{field}</option>);
                          })}
                        </select>
                    </div>
                    <div className="tag-search__create">
                        <Link className="tag-search__create-button" to="/tag/create" disabled={!canCreateTags}>Create a new tag</Link>
                        {
                            canCreateTags
                                ?
                                false
                                :
                                <i className="i-info-grey"
                                   data-tip='You do not have permission to create tags.<br/>If you think this is a mistake please contact central production'>
                                </i>
                        }
                    </div>
                </div>
                {this.renderPageNavigator()}
                <div className="tag-search__suggestions">
                    <TagsList tags={this.state.tags} sections={this.props.sections} sortBy={this.sortBy} />
                </div>
                {this.renderPageNavigator()}
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
    sections: state.sections,
    config: state.config
  };
}

function mapDispatchToProps(dispatch) {
  return {
    sectionActions: bindActionCreators(Object.assign({}, getSections), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(TagSearch);
