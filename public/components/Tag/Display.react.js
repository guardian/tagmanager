import React from 'react';
import TagEdit from './TagEdit.react';
import TypeSelect from '../utils/TypeSelect.react';
import SaveButton from '../utils/SaveButton.react';
import TagValidationErrors from './TagValidation.react';
import {validateTag} from '../../util/validateTag';

class TagDisplay extends React.Component {

    constructor(props) {
        super(props);

        this.isTagDirty = this.isTagDirty.bind(this);
        this.isTagFetched = this.isTagFetched.bind(this);
        this.isTagValid = this.isTagValid.bind(this);

    }

    componentDidMount() {
      if (!this.isTagFetched()) {
        this.props.tagActions.getTag(this.props.routeParams.tagId);
      }

      if (!this.props.sections || !this.props.sections.length) {
        this.props.sectionActions.getSections();
      }
    }

    saveTag() {
      this.props.tagActions.saveTag(this.props.tag);
    }

    resetTag() {
      this.props.tagActions.getTag(this.props.routeParams.tagId);
    }

    isTagDirty() {
      return this.props.saveState === 'SAVE_STATE_DIRTY';
    }

    isTagFetched() {
      return this.props.tag && (this.props.tag.id === parseInt(this.props.routeParams.tagId, 10));
    }

    isTagValid() {
      return !validateTag(this.props.tag).length;
    }

    render () {
      if (!this.isTagFetched()) {
        return (
          <div>Fetching Tag</div>
        );
      }

      return (
        <div className="tag">
          <div className="tag__columns-wrapper">
            <div className="tag__column--sidebar">
              <div className="tag-edit__input-group">
                <label className="tag-edit__input-group__header">Tag Type</label>
                <TypeSelect selectedType={this.props.tag.type} forceDisabled={true}/>
              </div>
              <TagEdit tag={this.props.tag} sections={this.props.sections} updateTag={this.props.tagActions.updateTag} />
              <TagValidationErrors validations={validateTag(this.props.tag)} />
            </div>
            <div className="tag__column">
              Column 2
            </div>
            <div className="tag__column">
              Column 3
            </div>
          </div>
          <SaveButton isHidden={!this.isTagDirty() || !this.isTagValid()} onSaveClick={this.saveTag.bind(this)} onResetClick={this.resetTag.bind(this)}/>
        </div>
      );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getTag from '../../actions/getTag';
import * as updateTag from '../../actions/updateTag';
import * as saveTag from '../../actions/saveTag';
import * as getSections from '../../actions/getSections';

function mapStateToProps(state) {
  return {
    tag: state.tag,
    sections: state.sections,
    saveState: state.saveState
  };
}

function mapDispatchToProps(dispatch) {
  return {
    tagActions: bindActionCreators(Object.assign({}, getTag, updateTag, saveTag), dispatch),
    sectionActions: bindActionCreators(Object.assign({}, getSections), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(TagDisplay);
