import React from 'react';
import {creatableTags} from '../../constants/tagTypes.js';

export default class TypeSelect extends React.Component {

  constructor(props) {
    super(props);
  }

  render () {

    if (!creatableTags || !creatableTags) {
      return (
        <select disabled="true">
          <option>Fetching types...</option>
        </select>
      );
    }

    return (
      <select value={this.props.selectedType} onChange={this.props.onChange} disabled={!!this.props.forceDisabled}>
        {!this.props.selectedType ? <option></option> : false}
        {creatableTags.sort((a, b) => {return a > b ? 1 : -1;}).map(function(type) {
          return (
            <option value={type} key={type}>{type}</option>
          );
        })}
      </select>
    );
  }
}
