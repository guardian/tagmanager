import React from 'react';
import CapiClient from '../util/CapiClient';
import debounce from 'lodash.debounce';
import ContentList from './ContentList/ContentList';
import BatchTagStatus from './BatchTagStatus/BatchTagStatus';

const CAPI_PAGE_SIZE = 200

export class BatchTag extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          searchString: '',
          content: [],
          selectedContent: [],
          resultsCount: 0
        };

        this.capiClient = CapiClient(props.config.capiUrl, props.config.capiKey);
        this.searchContent = debounce(this.searchContent.bind(this), 500);
    }

    searchFieldChange(e) {
      const searchString = e.target.value;

      this.setState({
        searchString: searchString
      });

      this.searchContent(searchString);
    }

    searchContent(searchString) {

      var self = this;

      this.capiClient.searchContent(searchString || this.state.searchString, {
        'page-size': CAPI_PAGE_SIZE,
        'show-tags': 'all'
      }).then(function(resp) {
          self.setState({
            content: resp.response.results,
            resultsCount: resp.response.total,
            selectedContent: []
          });
      }).fail(function(err, msg) {
          console.log('failed', err, msg);
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
        selectedContent: this.state.content.map(content => content.id)
      });
    }

    deselectAllContent() {
      this.setState({
        selectedContent: []
      });
    }

    renderTooManyResults() {
      if (this.state.resultsCount <= CAPI_PAGE_SIZE) {
        return false;
      }

      return (
        <div className="batch-tag__error">
          Over {CAPI_PAGE_SIZE} results found, please refine the search.
        </div>
      );
    }

    render () {
        return (
            <div className="batch-tag">
                <div className="batch-tag__filters">
                    <div className="batch-tag__filters__group">
                        <label>Search by name</label>
                        <input className="batch-tag__input" type="text" value={this.state.searchString} onChange={this.searchFieldChange.bind(this)} />
                    </div>
                    <div className="batch-tag__filters__group">
                        <span onClick={this.selectAllContent.bind(this)}>Select All</span>
                        <span onClick={this.deselectAllContent.bind(this)}>Unselect All</span>
                    </div>
                </div>
                {this.renderTooManyResults()}
                <div className="batch-tag__content">
                  <ContentList
                    content={this.state.content}
                    selectedContent={this.state.selectedContent}
                    contentClicked={this.toggleContentSelected.bind(this)} />
                </div>
                <div className="batch-tag__status">
                  <BatchTagStatus selectedContent={this.state.selectedContent} />
                </div>
            </div>
        );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as updateTag from '../actions/updateTag';

function mapStateToProps(state) {
  return {
    config: state.config
  };
}

function mapDispatchToProps(dispatch) {
  return {
    tagActions: bindActionCreators(Object.assign({}, updateTag), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(BatchTag);
