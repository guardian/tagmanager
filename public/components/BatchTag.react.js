import React from 'react';
import ContentList from './ContentList/ContentList';
import BatchTagStatus from './BatchTagStatus/BatchTagStatus';
import BatchFilters from './BatchTag/BatchFilters.react';
import R from 'ramda';

const CAPI_PAGE_SIZE = 200;

export class BatchTag extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          selectedContent: [],
          activeFilters: {},
          showFilters: false
        };

        this.searchContent = this.searchContent.bind(this);
    }

    searchFieldChange(e) {
      this.searchContent(e.target.value);
    }

    searchContent(searchString, filters) {

      const applyFilters = this.state.showFilters ? filters || this.props.capiSearch.filters : {};

      const params = Object.assign({}, applyFilters, {
        'show-tags': 'all',
        'page-size': CAPI_PAGE_SIZE
      });

      this.props.capiActions.searchCapi(searchString, params);
    }

    applyFilters(filters) {
      this.props.capiActions.updateFilters(filters);
      this.searchContent(this.props.capiSearch.searchTerm, filters);
    }

    toggleFilters() {
      this.setState({
        showFilters: !this.state.showFilters
      });
    }

    toggleContentSelected(content) {
      if (this.state.selectedContent.indexOf(content.id) === -1) {
        this.setState({
          selectedContent: this.state.selectedContent.concat([content.id])
        });
      } else {
        this.setState({
          selectedContent: this.state.selectedContent.filter(selectedContentId => selectedContentId !== content.id)
        });
      }
    }

    selectAllContent() {
      this.setState({
        selectedContent: this.props.capiSearch.results.map(content => content.id)
      });
    }

    deselectAllContent() {
      this.setState({
        selectedContent: []
      });
    }

    toggleAllSelected() {
      const notSelectedResults = R.exclude(R.any(this.state.selectedContent));

      if (notSelectedResults.length) {
        this.selectAllContent();
      } else {
        this.deselectAllContent();
      }
    }

    renderTooManyResults() {

      if (!this.props.capiSearch.count || this.props.capiSearch.count <= CAPI_PAGE_SIZE) {
        return false;
      }

      return (
        <div className="batch-tag__error">
          Over {CAPI_PAGE_SIZE} results found, please refine the search.
        </div>
      );
    }

    renderSearchStatus() {

      if (this.props.capiSearch.fetchState === 'FETCH_STATE_DIRTY') {
        return (
          <div className="batch-tag__info">
            Searching...
          </div>
        );
      }

      if (this.props.capiSearch.fetchState === 'FETCH_STATE_CLEAN' && this.props.capiSearch.results.length === 0) {
        return (
          <div className="batch-tag__error">
            No results found
          </div>
        );
      }

      return false;
    }

    render () {

        return (
            <div className="batch-tag">
                <div className="batch-tag__filters">
                    <div className="batch-tag__filters__group">
                        <label>Search by name</label>
                        <input className="batch-tag__input" type="text" value={this.props.capiSearch.searchTerm || ''} onChange={this.searchFieldChange.bind(this)} />
                    </div>
                    <div className="batch-tag__show-filters" onClick={this.toggleFilters.bind(this)}>
                      { this.state.showFilters ? 'Hide Filters' : 'Show Filters'}
                    </div>
                </div>
                {this.state.showFilters ? <BatchFilters filters={this.props.capiSearch.filters || {}} updateFilters={this.applyFilters.bind(this)}/> : false}
                {this.renderSearchStatus()}
                {this.renderTooManyResults()}
                <div className="batch-tag__content">
                  <ContentList
                    content={this.props.capiSearch.results}
                    selectedContent={this.state.selectedContent}
                    contentClicked={this.toggleContentSelected.bind(this)}
                    toggleAllSelected={this.toggleAllSelected.bind(this)}
                   />
                </div>
                <div className="batch-tag__status">
                  <BatchTagStatus
                    selectedContent={this.state.selectedContent}
                    onAddTagToContentTop={this.onAddTagToContentTop}
                    onAddTagToContentBottom={this.onAddTagToContentBottom}
                    onRemoveTagFromContent={this.onRemoveTagFromContent}
                  />
                </div>
            </div>
        );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as searchCapi from '../actions/CapiActions/searchCapi';

function mapStateToProps(state) {
  return {
    config: state.config,
    capiSearch: state.capiSearch
  };
}

function mapDispatchToProps(dispatch) {
  return {
    capiActions: bindActionCreators(Object.assign({}, searchCapi), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(BatchTag);
