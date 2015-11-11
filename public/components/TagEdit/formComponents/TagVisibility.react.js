import React from 'react';

export default class TagVisibilityEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  toggleHidden(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      hidden: !this.props.tag.hidden
    }));
  }

  toggleSensitive(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      legallySensitive: !this.props.tag.legallySensitive
    }));
  }

  render () {
    if (!this.props.tag) {
      return false;
    }

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Visibility Control</label>
        <div>
          <input type="checkbox" onChange={this.toggleHidden.bind(this)} checked={this.props.tag.hidden}/><label>Hidden</label>
        </div>
        <div>
          <input type="checkbox" onChange={this.toggleSensitive.bind(this)} checked={this.props.tag.legallySensitive}/><label>Legally Sensitive</label>
        </div>
      </div>
    );
  }
}