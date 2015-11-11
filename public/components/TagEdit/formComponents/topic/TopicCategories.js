import React from 'react';
import {topicCategories} from '../../../../constants/topicCategories';

export default class TopicCategories extends React.Component {

  constructor(props) {
    super(props);
  }

  render () {

    return (
      <select value={this.props.selectedCategory} onChange={this.props.onChange} disabled={!!this.props.forceDisabled}>
        {!this.props.selectedCategory ? <option></option> : false}
        {topicCategories.sort((a, b) => {return a > b ? 1 : -1;}).map(function(type) {
          return (
            <option value={type} key={type}>{type}</option>
          );
        })}
      </select>
    );
  }
}
