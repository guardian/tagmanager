import React from 'react';

export default class SectionSelect extends React.Component {

  constructor(props) {
    super(props);
  }

  render () {

    if (!this.props.sections || !this.props.sections.length) {
      return (
        <select disabled="true">
          <option>Fetching section names...</option>
        </select>
      );
    }

    return (
      <select value={this.props.selectedId} onChange={this.props.onChange}>
        {!this.props.selectedId || this.props.showBlank ? <option value={false}></option> : false}
        {this.props.sections.sort((a, b) => {return a.name > b.name ? 1 : -1;}).map(function(s) {
          return (
            <option value={s.id} key={s.id} >{s.name}</option>
          );
        })}
      </select>
    );
  }
}
