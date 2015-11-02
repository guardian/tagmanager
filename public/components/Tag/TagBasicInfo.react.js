import React from 'react';

export default class TagBasicInfo extends React.Component {

  constructor(props) {
    super(props);
  }

  onUpdateInternalName(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      internalName: e.target.value
    }));
  }

  onUpdateExternalName(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      externalName: e.target.value,
      slug: e.target.value.replace(/[^a-z0-9-]+$/i, '-')
    }));
  }

  render () {
    if (!this.props.tag) {
      console.log('TagEdit loaded without tag provided');
      return false;
    }

    return (
      <div className="tag-edit__input-group">
        <div className="tag-edit__name">
          <label>Internal Name</label>
          <input className="tag-edit__input" type="text" value={this.props.tag.internalName} onChange={this.onUpdateInternalName.bind(this)}/>
          <div className="tag-edit__linked-field">
            <div className="tag-edit__linked-field__link--junction"></div>
            <div className="tag-edit__linked-field__lock"></div>
            <label>External Name</label>
            <input type="text" disabled="true" value={this.props.tag.externalName} onChange={this.onUpdateExternalName.bind(this)}/>
          </div>
        </div>
      </div>
    );
  }
}
