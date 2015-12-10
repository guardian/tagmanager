import React from 'react';

export default class TypeSelect extends React.Component {

  constructor(props) {
    super(props);
  }

  render () {

    if (!this.props.types) {
      return (
        <select disabled="true">
          <option>No Tag Types Found...</option>
        </select>
      );
    }

    return (
      <select value={this.props.selectedType} onChange={this.props.onChange} disabled={!!this.props.forceDisabled}>
        {!this.props.selectedType ? <option></option> : false}
        {this.props.types.sort((a, b) => {return a > b ? 1 : -1;}).map(function(type) {
          return (
            <option value={type} key={this.props.selectedType + '_' + type}>{type}</option>
          );
        }, this)}
      </select>
    );
  }
}
