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

    const sections = this.props.isMicrosite ? this.props.sections.filter(sec => sec.isMicrosite === true) : this.props.sections.filter(sec => sec.isMicrosite === false);

    return (
      <select value={this.props.selectedId} onChange={this.props.onChange}>
        {!this.props.selectedId || this.props.showBlank ? <option value={false}></option> : false}
        {sections.sort((a, b) => {return a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1;}).map(function(s) {
          return (
            <option value={s.id} key={s.id} >{s.name}</option>
          );
        })}
      </select>
    );
  }
}
