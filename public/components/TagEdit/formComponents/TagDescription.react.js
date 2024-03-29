import React from 'react';
import { RichTextEditor, customMultiBlockTextConfig, transformToLegacyMarkup } from '@guardian/prosemirror-editor';


const config = customMultiBlockTextConfig({ 
  allowedNodes: ["text", "paragraph", "hard_break"],
  allowedMarks: ["strong", "em", "link"]
})

export default class TagDescription extends React.Component {

  constructor(props) {
    super(props);
  }

  updateDescription(html) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      description: transformToLegacyMarkup(html)
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
          <RichTextEditor 
            value={this.props.tag.description || ''}
            onUpdate={this.updateDescription.bind(this)}
            config={config}
            disabled={!this.props.tagEditable}
          />
        </div>
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
