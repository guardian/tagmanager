import React from 'react';
import { Link } from 'react-router';
import tagManagerApi from '../util/tagManagerApi';
import TagsList from './TagList/TagList.react';

const searchFields = {
  'Internal Name': 'internalName',
  'External Name': 'externalName',
  'ID': 'id',
  'Type': 'type',
  'Path': 'path'
};

export class TagSearch extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          searchString: '',
          tags: [],
          sortResultsBy: 'internalName',
          searchFieldName:'internalName'
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

    searchTags(searchString, searchFieldName, sortBy) {

      var self = this;

      this.setState({
        searchString: searchString !== undefined ? searchString : this.state.searchString,
        sortBy: sortBy !== undefined ? sortBy : this.state.sortBy,
        searchFieldName: searchFieldName !== undefined ? searchFieldName : this.state.searchFieldName
      });

      tagManagerApi.searchTags(searchString, {
        searchFieldName: searchFieldName,
        orderByField: sortBy
      })
      .then(function(resp) {
          self.setState({tags: resp});
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
      this.searchTags(this.state.searchString, fieldName);
    }

    render () {
        return (
            <div className="tag-search">
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

export default connect(mapStateToProps, mapDispatchToProps)(TagSearch);
