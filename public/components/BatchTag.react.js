import React from 'react';
import CapiClient from '../util/CapiClient';
import debounce from 'lodash.debounce';
import ContentList from './ContentList/ContentList';
import BatchTagStatus from './BatchTagStatus/BatchTagStatus';

export class BatchTag extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          searchString: '',
          content: [],
          selectedContent: []
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
        'page-size': 200,
        'show-tags': 'all'
      }).then(function(resp) {
          self.setState({
            content: resp.response.results,
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
