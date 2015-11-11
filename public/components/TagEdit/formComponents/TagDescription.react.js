import React from 'react';

export default class TagVisibilityEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  updateDescription(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      description: e.target.value
    }));
  }

  render () {
    if (!this.props.tag) {
      return false;
    }

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Description</label>
        <div>
          <textarea onChange={this.updateDescription.bind(this)} value={this.props.tag.description}/>
        </div>
      </div>
    );
  }
}
