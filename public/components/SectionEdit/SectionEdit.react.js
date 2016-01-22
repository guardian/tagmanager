import React from 'react';
import SectionName from './SectionName.react';

export default class SectionEdit extends React.Component {

    constructor(props) {
      super(props);
    }

    render () {

      if (!this.props.section) {
        return false;
      }

      return (
        <SectionName section={this.props.section} updateSection={this.props.updateSection} pathLocked={this.props.pathLocked} />
      );
    }
}
