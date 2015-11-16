import React from 'react';
import tagManagerApi from '../../util/tagManagerApi';
import debounce from 'lodash.debounce';

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

      this.performSearch(searchTerm);
    }

    onClickTag(tag) {
      this.props.onTagClick(tag);
      this.setState(BLANK_STATE);
    }

    performSearch(searchTerm) {
      tagManagerApi.searchTags(searchTerm || this.state.searchTerm)
        .then((tags) => {
          this.setState({
            suggestions: tags
          });
        }).fail(error => {
          console.log('Could not get suggestions for: ', searchTerm || this.state.searchTerm, error);
        });
    }

    render() {
      return (
        <div className={this.props.showResultsAbove ? 'tag-select--results-above' : 'tag-select'}>
            <div>
              <input className="tag-select__input" type="text" autoFocus={true} value={this.state.tagSearchTerm} onChange={this.onUpdateSearchField.bind(this)}/>
            </div>
            <div className="tag-select__suggestions">
              <ul>
                {this.state.suggestions.map(tag => {
                  return <li key={tag.id} onClick={this.onClickTag.bind(this, tag)}>{tag.internalName}</li>;
                })}
              </ul>
            </div>
        </div>
      );
    }
}
