import React from 'react';

export default class ReferenceTypeSelect extends React.Component {

  constructor(props) {
    super(props);
  }

  render () {

    if (!this.props.referenceTypes || !this.props.referenceTypes.length) {
      return (
        <select disabled="true">
          <option>Fetching reference types...</option>
        </select>
      );
    }

    return (
      <select value={this.props.selectedType} onChange={this.props.onChange}>
        {!this.props.selectedType ? <option></option> : false}
        {this.props.referenceTypes.sort((a, b) => {return a.displayName > b.displayName ? 1 : -1;}).map(function(t) {
          return (
            <option value={t.typeName} key={t.typeName}>{t.displayName}</option>
          );
        })}
      </select>
    );
  }
}
