import React from 'react';

export default class TagSponsorships extends React.Component {

  constructor(props) {
    super(props);
  }

  render () {
    if (!this.props.tag) {
      console.log('TagContext loaded without tag provided');
      return false;
    }

    return (
      <div className="tag-context__item">
        <div className="tag-context__header">Active sponsorships</div>
        No sponsorships found
      </div>
    );
  }
}