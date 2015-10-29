import React from 'react';

export default class TagEdit extends React.Component {

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
        externalName: e.target.value
      }));
    }

    render () {
      if (!this.props.tag) {
        console.log('TagEdit loaded without tag provided');
        return false;
      }

      return (
        <div className="tag-edit">
          <div className="tag-edit__form">
            <div className="tag-edit__form__input-group">
              <label>Internal Name</label>
              <input type="text" value={this.props.tag.internalName} onChange={this.onUpdateInternalName.bind(this)}/>
            </div>
            <div className="tag-edit__form__input-group">
              <label>External Name</label>
              <input type="text" value={this.props.tag.externalName} onChange={this.onUpdateExternalName.bind(this)}/>
            </div>
          </div>
        </div>
      );
    }
}
