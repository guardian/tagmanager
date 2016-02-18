import React from 'react';
import tagManagerApi from '../../util/tagManagerApi';
import debounce from 'lodash.debounce';
import * as tagTypes from '../../constants/tagTypes';

const BLANK_STATE = {
  selectedTag: undefined,
  tagSearchTerm: '',
  suggestions: []
};

export default class TagSelect extends React.Component {

    constructor(props) {
        super(props);

        this.state = BLANK_STATE;
        this.performSearch = debounce(this.performSearch.bind(this), 300);

    }

    onUpdateSearchField(e) {
      const searchTerm = e.target.value;
      this.setState({
        tagSearchTerm: searchTerm
      });

      if (searchTerm.length !== 0) {
        this.performSearch(searchTerm);
      } else {
         this.setState({
           suggestions: []
         });
      }
    }

    onClickTag(tag) {
      this.props.onTagClick(tag);
      this.setState(BLANK_STATE);
    }

    performSearch(searchTerm) {
      tagManagerApi.searchTags(searchTerm || this.state.searchTerm, {tagType: this.props.tagType})
        .then((tags) => {
          this.setState({
            suggestions: tags
          });
        }).fail(error => {
          console.log('Could not get suggestions for: ', searchTerm || this.state.searchTerm, error);
        });
    }

    isAllowedTagType(tagType) {
      if (this.props.blockedTagTypes) {
        return this.props.blockedTagTypes.indexOf(tagType) === -1;
      }
      return true;
    }

    renderSuggestions() {
      if (!this.state.suggestions.length) {
        return false;
      }

      return (
        <div className="tag-select__suggestions">
          <ul>
            {this.state.suggestions.filter(tag => this.isAllowedTagType(tag.type)).map(tag => {
              const tagTypeKey = Object.keys(tagTypes).filter((tagTypeKey) => {
                return tagTypes[tagTypeKey].name === tag.type;
              })[0];

              return (
                <li key={tag.id} onClick={this.onClickTag.bind(this, tag)}>
                  <div className="tag-select__name">{tag.internalName}</div>
                  <div className="tag-select__tag-type">
                    {tagTypes[tagTypeKey] ? tagTypes[tagTypeKey].displayName : ''}
                  </div>
                </li>
              );
            })}
          </ul>
        </div>
      );
    }

    render() {
      return (
        <div className={this.props.showResultsAbove ? 'tag-select--results-above' : 'tag-select'}>
            <div>
              <input
                className="tag-select__input"
                type="text" autoFocus={true}
                value={this.state.tagSearchTerm}
                onChange={this.onUpdateSearchField.bind(this)}
                disabled={this.props.disabled}/>
            </div>
            {this.renderSuggestions()}
        </div>
      );
    }
}
