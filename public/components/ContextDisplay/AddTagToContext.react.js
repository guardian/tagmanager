import React from 'react';
import tagManagerApi from '../../util/tagManagerApi';
import debounce from 'lodash.debounce';

export default class AddTagToContext extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          searchTerm: '',
          expanded: false,
          suggestions: [],
          selectedTag: undefined
        };

        this.performSearch = debounce(this.performSearch.bind(this), 500);
    }

    contract() {
      this.setState({
        expanded: false,
        searchTerm: '',
        suggestions: []
      });
    }

    expand() {
      this.setState({
        expanded: true
      });
    }

    performSearch() {
      tagManagerApi.searchTags(this.state.searchTerm)
        .then((tags) => {
          this.setState({
            suggestions: tags
          });
        }).fail(error => {
          console.log('Could not get suggestions for: ', this.state.searchTerm, error);
        });
    }

    onAddTag(tag) {
      this.props.onAddTag(tag.id);
      this.contract();
    }

    onUpdateSearchField(e) {
      var searchTerm = e.target.value;
      this.setState({
        searchTerm: searchTerm
      });

      this.performSearch();
    }

    render () {

      if (!this.state.expanded) {
        return (
          <div className="context__add" onClick={this.expand.bind(this)}>
            <i className="i-plus" />
          </div>
        );
      }

      return (
        <div className="context__add--expanded">
          <input className="context__add__input" type="text" autoFocus={true} value={this.state.inputValue} onChange={this.onUpdateSearchField.bind(this)}/>
          <i className="i-cross" onClick={this.contract.bind(this)}/>
          <div className="context__add__suggestions">
            <ul>
              {this.state.suggestions.map(tag => {
                return <li key={tag.id} onClick={this.onAddTag.bind(this, tag)}>{tag.internalName}</li>;
              })}
            </ul>
          </div>

        </div>
      );
    }
}
