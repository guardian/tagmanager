import React from 'react';
import TagSelect from '../utils/TagSelect';

const BLANK_STATE = {
  referenceValue: '',
  selectedTag: undefined
};

export default class AddTagMapping extends React.Component {

    constructor(props) {
        super(props);

        this.state = BLANK_STATE;
    }

    updateSelectedTag(tag) {
      this.setState({
        selectedTag: tag
      });
    }

    updateReferenceValue(e) {
      this.setState({
        referenceValue: e.target.value
      });
    }

    addReferenceValue() {

      const existingReferences = this.state.selectedTag.externalReferences ? this.state.selectedTag.externalReferences : [];

      const newTag = Object.assign({}, this.state.selectedTag, {
        externalReferences: existingReferences.concat([
          {
            type: this.props.selectedType,
            value: this.state.referenceValue
          }
        ])
      });

      this.setState(BLANK_STATE);

      this.props.saveTag(newTag);
    }

    renderTagInput() {
      if (this.state.selectedTag) {
        return (
          <div className="mapping__add__selectedtag">
            {this.state.selectedTag.internalName}
            <i className="i-cross" onClick={this.updateSelectedTag.bind(this, undefined)}/>
          </div>
        );
      } else {
        return <TagSelect onTagClick={this.updateSelectedTag.bind(this)}/>;
      }
    }

    render() {

      if (!this.props.selectedType) {
        return false;
      }

      return (
        <div className="mapping__add">
          <div>
            <label className="mapping__add__title">Add a {this.props.selectedType}</label>
          </div>

          <div className="mapping__add__block">
            <label className="mapping__add__title">Tag</label>
            {this.renderTagInput()}
          </div>
          <div className="mapping__add__block">
            <label className="mapping__add__title">Value</label>
            <input value={this.state.referenceValue} onChange={this.updateReferenceValue.bind(this)}/>
          </div>
          <input type="submit" onClick={this.addReferenceValue.bind(this)}/>
        </div>
      );
    }
}
