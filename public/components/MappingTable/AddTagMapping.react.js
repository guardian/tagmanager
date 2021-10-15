import React from 'react';
import TagSelect from '../utils/TagSelect';
import MappingsWarning from '../utils/MappingsWarning.react';
import {getByTag} from '../../util/capiClient';

const BLANK_STATE = {
  referenceValue: '',
  selectedTag: undefined,
  tagCapiUsages: 0
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

      if (tag) {
        getByTag(tag, {
          'page-size': 0
        }).then(res => {
          this.setState({
            tagCapiUsages: res.response.total
          });
        });
      } else {
        this.setState({
            tagCapiUsages: 0
        });
      }
    }

    updateReferenceValue(e) {
      this.setState({
        referenceValue: e.target.value
      });
    }

    addReferenceValue() {

      const existingReferences = this.state.selectedTag.externalReferences ? this.state.selectedTag.externalReferences : [];

      const newReference = {
        type: this.props.selectedType.typeName,
        value: this.state.referenceValue
      };

      if (this.props.selectedType.capiType) {
        newReference.capiType = this.props.selectedType.capiType;
      }

      const newTag = Object.assign({}, this.state.selectedTag, {
        externalReferences: existingReferences.concat([newReference])
      });

      this.setState(BLANK_STATE);

      this.props.saveTag(newTag);
    }

    renderTagInput() {
      if (this.state.selectedTag) {
        return (
          <div className="mapping__add__selectedtag">
            {this.state.selectedTag.internalName}
            <i className="i-cross clickable-icon" onClick={this.updateSelectedTag.bind(this, undefined)}/>
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
            <label className="mapping__add__title">Add a {this.props.selectedType.typeName}</label>
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
          <MappingsWarning capiUsages={this.state.tagCapiUsages}/>
        </div>
      );
    }
}
