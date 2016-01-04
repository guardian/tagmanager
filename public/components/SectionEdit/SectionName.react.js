import React from 'react';

function slugify(text) {
  return text ? text.toLowerCase().replace(/[^a-z0-9-]/g, '-') : '';
}

export default class SectionName extends React.Component {

  constructor(props) {
    super(props);
  }

  onUpdateName(e) {
    this.props.updateSection(Object.assign({}, this.props.section, {
      name: e.target.value
    }));
  }

  render () {
    if (!this.props.section) {
      console.log('SectionEdit loaded without section provided');
      return false;
    }

    return (
      <div className="tag-edit__input-group">
        <div className="tag-edit__name">
          <label className="tag-edit__input-group__header">Name</label>
          <input className="tag-edit__input" type="text" value={this.props.section.name} onChange={this.onUpdateName.bind(this)}/>
          <div className="tag-edit__linked-field">
            <div className={"tag-edit__linked-field__link--junction"}></div>
            <div className={"tag-edit__linked-field__lock"}></div>
            <label>Path</label>
            <div className="tag-edit__linked-field__input-container">
              <input type="text" value={this.props.section.path} disabled="true" />
            </div>
          </div>
          <div className="tag-edit__linked-field">
            <div className={"tag-edit__linked-field__link--corner"}></div>
            <div className={"tag-edit__linked-field__lock"}></div>
            <label>Slug</label>
            <div className="tag-edit__linked-field__input-container">
              <input type="text" value={this.props.section.wordsForUrl} disabled="true"/>
            </div>
          </div>
        </div>
      </div>
    );
  }
}
