import React from 'react';
import ReactScribe from '../../utils/ReactScribe.react';
import { Editor } from '@guardian/prosemirror-editor';

export default class TagDescription extends React.Component {

  constructor(props) {
    super(props);
  }

  updateDescription(html) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      description: html
    }));
  }
  render () {
    if (!this.props.tag) {
      return false;
    }
    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Description/Profile</label>
        <div>
          <ReactScribe
            onChange={this.updateDescription.bind(this)}
            value={this.props.tag.description || ''}
            className="tag-edit__richtext"
            toolbarClassName="tag-edit__richtext__toolbar"
            toolbarItemClassName="tag-edit__richtext__toolbar__item"
            editorClassName="tag-edit__richtext__editor"
            disabled={!this.props.tagEditable}
          />
        </div>
        <Editor></Editor>
        <div className="tag-edit__profile-guidelines">
          <a
              href="https://docs.google.com/document/d/1iqgHNoGnglFjvL94meG4broH9xSiGyR87i_-fQpDrwA"
              target="_blank"
              rel="noopener noreferrer"
          >
              Writing a contributor profile? Please read the
              guidelines (here)!
          </a>
        </div>
      </div>
    );
  }
}
