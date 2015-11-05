import React from 'react';

export default class SectionSelect extends React.Component {

  constructor(props) {
    super(props);
  }

  render () {

    if (!this.props.types || !this.props.types.length) {
      return (
        <select disabled="true">
          <option>Fetching types...</option>
        </select>
      );
    }

    return (
      <select value={this.props.selectedType} onChange={this.props.onChange}>
        {!this.props.selectedType ? <option></option> : false}
        {this.props.types.sort((a, b) => {return a > b ? 1 : -1;}).map(function(type) {
          return (
            <option value={type} key={type}>{type}</option>
          );
        })}
      </select>
    );
  }
}
