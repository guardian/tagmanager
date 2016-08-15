import React from 'react';

import tagManagerApi from '../../util/tagManagerApi';

export default class PublicationSelect extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      publications: []
    }
  }

  fetchPublications() {
    tagManagerApi.searchTags('', {
      tagType: 'Publication'
    })
    .then((tags) => {
      this.setState({
        publications: tags
      });
    });
  }

  componentDidMount() {
    if (!this.state.publications || !this.state.publications.length) {
      this.fetchPublications();
    }
  }

  render () {

    if (!this.state.publications || !this.state.publications.length) {
      return (
        <select disabled="true">
          <option>Fetching publication names...</option>
        </select>
      );
    }

    return (
      <select value={this.props.selectedId || false} onChange={this.props.onChange} disabled={this.props.disabled}>
        {!this.props.selectedId || this.props.showBlank ? <option value={false}></option> : false}
        {this.state.publications.sort((a, b) => {return a.internalName > b.internalName ? 1 : -1;}).map(function(s) {
          return (
            <option value={s.id} key={s.id} >{s.internalName}</option>
          );
        })}
      </select>
    );
  }
}
