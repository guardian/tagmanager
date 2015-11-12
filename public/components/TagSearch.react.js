import React from 'react';
import { Link } from 'react-router';
import tagManagerApi from '../util/tagManagerApi';
import TagsList from './TagList/TagList.react';

export class TagSearch extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          searchString: '',
          tags: [],
          sortResultsBy: 'internalName'
        };

        this.sortBy = this.sortBy.bind(this);
        this.searchTags = this.searchTags.bind(this);

        if (!this.props.sections || !this.props.sections.length) {
          this.props.sectionActions.getSections();
        }

        this.searchTags();
    }

    searchTags(searchString, sortBy) {

      var self = this;

      tagManagerApi.searchTags(searchString, sortBy)
      .then(function(resp) {
          self.setState({tags: resp});
      }).fail(function(err, msg) {
          console.log('failed', err, msg);
      });

      this.setState({
        searchString: searchString,
        sortBy: sortBy
      });
    }

    handleChange(event) {
      this.searchTags(event.target.value, this.state.sortBy);
    }

    sortBy(fieldName) {
      this.searchTags(this.state.searchString, fieldName);
    }

    render () {
        return (
            <div className="tag-search">
                <div className="tag-search__filters">
                    <div className="tag-search__filters__group">
                        <label>Filter by name</label>
                        <input className="tag-search__input" type="text" value={this.state.searchString} onChange={this.handleChange.bind(this)} />
                    </div>

                    <Link className="tag-search__create" to="/tag/create">Create a new tag</Link>

                </div>
                <div className="tag-search__suggestions">
                    <TagsList tags={this.state.tags} sections={this.props.sections} sortBy={this.sortBy} />
                </div>
            </div>
        );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getSections from '../actions/getSections';

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

export default connect(mapStateToProps, mapDispatchToProps)(TagSearch);
