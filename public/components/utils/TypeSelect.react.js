import React from 'react';
import {creatableTypes} from '../../constants/tagTypes.js';

export default class TypeSelect extends React.Component {

  constructor(props) {
    super(props);
  }

  render () {

    if (!creatableTypes) {
      return (
        <select disabled="true">
          <option>Fetching types...</option>
        </select>
      );
    }

    return (
      <select value={this.props.selectedType} onChange={this.props.onChange} disabled={!!this.props.forceDisabled}>
        {!this.props.selectedType ? <option></option> : false}
        {creatableTypes.sort((a, b) => {return a > b ? 1 : -1;}).map(function(type) {
          return (
            <option value={type} key={this.props.selectedType + '_' + type}>{type}</option>
          );
        }, this)}
      </select>
    );
  }
}
