import React from 'react';
import ContentList from './ContentList/ContentList';
import PageNavigator from './utils/PageNavigator.react';
import BatchTagControls from './BatchTagControls/BatchTagControls';
import BatchFilters from './BatchTag/BatchFilters.react';
import R from 'ramda';

const CAPI_PAGE_SIZE = 10;
const PAGE_NAV_SPAN = 5;

export class BatchTag extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          selectedContent: [],

          showFilters: false
        };

        this.searchContent = this.searchContent.bind(this);
    }

    componentDidMount() {
      if (!this.props.sections || !this.props.sections.length) {
        this.props.sectionActions.getSections();
      }
    }

    searchFieldChange(type, e) {

      const searchTerm = type === 'searchTerm' ? e.target.value : this.props.capiSearch.searchTerm;
      const byline = type === 'byline' ? e.target.value : this.props.capiSearch.byline;
      this.searchContent(searchTerm, byline);
    }

    searchContent(searchString, byline, filters, page = 1) {
      this.props.capiActions.clearPages();
      this.state.selectedContent = [];

      const applyFilters = this.state.showFilters ? filters || this.props.capiSearch.filters : {};

      const params = Object.assign({}, applyFilters, {
        'show-tags': 'all',
        'page-size': CAPI_PAGE_SIZE,
        'page': page
      });

      this.props.capiActions.searchPreviewCapi(searchString, byline, params);
    }

    applyFilters(filters) {
      this.props.capiActions.updateFilters(filters);
      this.searchContent(this.props.capiSearch.searchTerm, this.props.capiSearch.byline, filters);
    }

    toggleFilters() {
      this.setState({
        showFilters: !this.state.showFilters
      });
    }

    toggleContentSelected(content) {
      const addSelectedContent = R.append(content.id);
      const removeSelectedContent = R.reject(R.equals(content.id));

      if (this.state.selectedContent.indexOf(content.id) === -1) {
        this.setState({
          selectedContent: addSelectedContent(this.state.selectedContent)
        });
      } else {
        this.setState({
          selectedContent: removeSelectedContent(this.state.selectedContent)
        });
      }
    }

    selectAllContentFromPage(page) {
      this.setState({
          selectedContent: R.union(this.state.selectedContent, page.filter(c => !!c.fields.internalComposerCode).map(c => c.id))
      });
    }

    deselectAllContentFromPage(page) {
      var newContent = R.difference(this.state.selectedContent, page.filter(c => !!c.fields.internalComposerCode).map(c => c.id));

      this.setState({
              selectedContent: newContent
      });
    }

    toggleAllSelected() {
      const currentPage = this.props.capiSearch.pages[this.props.capiSearch.currentPage];
      const notSelectedResults = R.difference(currentPage.filter(c => !!c.fields.internalComposerCode).map((content) => content.id), this.state.selectedContent);

      if (notSelectedResults.length) {
        this.selectAllContentFromPage(currentPage);
      } else {
        this.deselectAllContentFromPage(currentPage);
      }
    }

    renderSearchStatus() {
      if (this.props.capiSearch.pageRequestCount > 0) {
        return (
          <div className="batch-tag__info">
            Searching...
          </div>
        );
      }

      if (this.props.capiSearch.pageRequestCount === 0 && this.props.capiSearch.pages[1].length === 0) {
        return (
          <div className="batch-tag__error">
            No results found
          </div>
        );
      }

      return false;
    }

    pageSelectCallback(page) {
      const applyFilters = this.state.showFilters ? this.props.capiSearch.filters : {};

      const params = Object.assign({}, applyFilters, {
        'show-tags': 'all',
        'page-size': CAPI_PAGE_SIZE,
        'page': page
      });

      if (this.props.capiSearch.pages[page]) {
        // If we already have the page just used the cached version
        this.props.capiActions.switchPage(page);
      } else {
        this.props.capiActions.searchPreviewCapi(this.props.capiSearch.searchTerm, this.props.capiSearch.byline, params);
      }
    }

    renderPageNavigator() {
        var count = this.props.capiSearch.count;
        if (count > 0 && count > CAPI_PAGE_SIZE) {
            return (<PageNavigator
                        pageSelectCallback={this.pageSelectCallback.bind(this)}
                        currentPage={this.props.capiSearch.currentPage}
                        pageSpan={PAGE_NAV_SPAN}
                        lastPage={Math.ceil(this.props.capiSearch.count / CAPI_PAGE_SIZE)}/>);
        }

        return false;
    }

    renderContent() {
        if (this.props.capiSearch.count > 0) {
            return (<div className="batch-tag__content">
                  <ContentList
                    content={this.props.capiSearch.pages[this.props.capiSearch.currentPage]}
                    selectedContent={this.state.selectedContent}
                    contentClicked={this.toggleContentSelected.bind(this)}
                    toggleAllSelected={this.toggleAllSelected.bind(this)}
                   />
                   </div>);
        }

        return false;
    }

    render () {
        return (
            <div className="batch-tag">
                <div className="batch-tag__filters">
                    <div className="batch-tag__filters__group">
                        <label>Search by query</label>
                        <input className="batch-tag__input" type="text" value={this.props.capiSearch.searchTerm || ''} onChange={this.searchFieldChange.bind(this, 'searchTerm')} />
                    </div>
                    <div className="batch-tag__filters__group">
                        <label>Search by byline</label>
                        <input className="batch-tag__input" type="text" value={this.props.capiSearch.byline || ''} onChange={this.searchFieldChange.bind(this, 'byline')} />
                    </div>
                    <div className="batch-tag__show-filters" onClick={this.toggleFilters.bind(this)}>
                      { this.state.showFilters ? 'Hide Filters' : 'Show Filters'}
                    </div>
                </div>
                {this.state.showFilters ? <BatchFilters filters={this.props.capiSearch.filters || {}} updateFilters={this.applyFilters.bind(this)} sections={this.props.sections}/> : false}
                {this.renderSearchStatus()}

                {this.renderPageNavigator()}
                {this.renderContent()}
                {this.renderPageNavigator()}


                <div className="batch-tag__status">
                  <BatchTagControls
                    selectedContent={this.state.selectedContent}
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
import * as getSections from '../actions/SectionsActions/getSections';

function mapStateToProps(state) {
  return {
    config: state.config,
    capiSearch: state.capiSearch,
    sections: state.sections
  };
}

function mapDispatchToProps(dispatch) {
  return {
    capiActions: bindActionCreators(Object.assign({}, searchCapi), dispatch),
    sectionActions: bindActionCreators(Object.assign({}, getSections), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(BatchTag);
