import React from 'react';
import {topicCategories} from '../../../../constants/topicCategories';
import R from 'ramda';

export default class TopicCategories extends React.Component {

  constructor(props) {
    super(props);

    this.isSelected = this.isSelected.bind(this);
    this.addCategory = this.addCategory.bind(this);
    this.removeCategory = this.removeCategory.bind(this);
  }

  removeCategory(category) {
    const removeCategory = R.reject(R.equals(category));

    this.props.onChange(removeCategory(this.props.selectedCategories));
  }

  addCategory(category) {
    const addCategory = R.append(category);

    if (this.props.selectedCategories) {
      this.props.onChange(addCategory(this.props.selectedCategories));
    } else {
      this.props.onChange([category]);
    }
  }

  onChecked(category) {
    if (this.isSelected(category)) {
      this.removeCategory(category);
    } else {
      this.addCategory(category);
    }
  }

  isSelected(category) {
    return !!this.props.selectedCategories && this.props.selectedCategories.indexOf(category) !== -1;
  }

  render () {

    return (
      <div className="tag-edit__visibility">
        {topicCategories.map(category => {
          return (
            <div className="tag-edit__field" key={category}>
              <input type="checkbox" checked={this.isSelected(category)} onChange={this.onChecked.bind(this, category)}/>
              <label className="tag-edit__label"> {category}</label>
            </div>
          );
        })}
      </div>
    );
  }
}
