import React from 'react';
import TagEdit from '../TagEdit/TagEdit.react';
import TagContext from '../TagContext/TagContext.react';
import SaveButton from '../utils/SaveButton.react';
import TypeSelect from './TypeSelect.react';
import TagValidationErrors from './TagValidation.react';
import {validateTag} from '../../util/validateTag';
import CapiStats from '../CapiStats/CapiStats.react';
import JobStatus from '../JobStatus/JobStatus.react';
import TagAudit from './TagAudit.react';
import ConfirmButton from '../utils/ConfirmButton.react';

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

      if (!this.props.referenceTypes || !this.props.referenceTypes.length) {
        this.props.referenceTypeActions.getReferenceTypes();
      }
    }

    UNSAFE_componentWillReceiveProps(props) {
      if (props.tag.id !== parseInt(props.routeParams.tagId, 10)) {
        props.tagActions.getTag(parseInt(props.routeParams.tagId, 10));
      }

      if (!props.sections && this.props.sections) {
        this.props.sectionActions.getSections();
      }
    }

    saveTag() {
      this.props.tagActions.saveTag(this.props.tag);
    }

    resetTag() {
      this.props.tagActions.getTag(this.props.routeParams.tagId);
    }

    deleteTag() {
      this.props.tagActions.deleteTag(this.props.tag);
    }

    isTagDirty() {
      return this.props.saveState !== 'SAVE_STATE_CLEAN';
    }

    isTagFetched() {
      return this.props.tag && (this.props.tag.id === parseInt(this.props.routeParams.tagId, 10));
    }

    isTagValid() {
      return !validateTag(this.props.tag).length;
    }

    renderDeleteButton() {
      if (this.props.config.permissions["tag_admin"]) {
        return <ConfirmButton className="tag__delete" onClick={this.deleteTag.bind(this)} buttonText="Delete Tag" />
      } else {
        return (
          <div>
            <ConfirmButton className="tag__delete tag__delete--disabled" disabled={true} buttonText="Delete Tag" />
            <span className="tag-edit__label">You do not have permission to delete this tag.</span>
          </div>
        )
      }
    }

    renderSaveBanner() {
      if (this.props.tagEditable) {
        return <SaveButton saveState={this.props.saveState} isHidden={!this.isTagDirty() || !this.isTagValid()}
                           onSaveClick={this.saveTag.bind(this)}
                           onResetClick={this.resetTag.bind(this)}/>
      }
    }

    renderPermissionsWarningBar() {
      if (!this.props.tagEditable) {
          return (
            <div className="warning-bar">
              You do not have permission to edit this tag
            </div>
          );
      }

      return false;
    }

    render () {
      if (!this.isTagFetched()) {
        return (
          <div>Fetching Tag</div>
        );
      }

      return (
        <div className="tag">
          {this.renderPermissionsWarningBar()}
          <div className="tag__columns-wrapper">
            <div className="tag__column--sidebar">
              <div className="tag-edit__input-group">
                <label className="tag-edit__input-group__header">Tag Type</label>
                <TypeSelect selectedType={this.props.tag.type} types={this.props.config.tagTypes} forceDisabled={true}/>
              </div>
                <div className="tag-edit__input-group">
                    <label className="tag-edit__input-group__header">Keyword Type</label>
                    <TypeSelect selectedType={this.props.tag.keywords} types={this.props.config.keywordTypes} forceDisabled={true}/>
                </div>
              <TagEdit tag={this.props.tag} sections={this.props.sections} updateTag={this.props.tagActions.updateTag} pathLocked={true} tagEditable={this.props.tagEditable}/>
              <TagValidationErrors validations={validateTag(this.props.tag)} />
              {this.renderDeleteButton()}
            </div>
            <div className="tag__column">
              <div className="tag__column--header">Relationships</div>
              <TagContext tag={this.props.tag} updateTag={this.props.tagActions.updateTag} referenceTypes={this.props.referenceTypes} tagEditable={this.props.tagEditable}/>
            </div>
            <div className="tag__column">
              <div className="tag__column--header">Usage</div>
              <CapiStats tag={this.props.tag} config={this.props.config} />
              <JobStatus tagId={this.props.tag.id} config={this.props.config}/>
              <TagAudit tagId={this.props.tag.id} saveState={this.props.saveState}/>
            </div>
          </div>
          {this.renderSaveBanner()}
        </div>
      );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getTag from '../../actions/TagActions/getTag';
import * as updateTag from '../../actions/TagActions/updateTag';
import * as saveTag from '../../actions/TagActions/saveTag';
import * as deleteTag from '../../actions/TagActions/deleteTag';
import * as getSections from '../../actions/SectionsActions/getSections';
import * as getReferenceTypes from '../../actions/ReferenceTypeActions/getReferenceTypes';

function mapStateToProps(state) {
  return {
    tag: state.tag,
    tagEditable: state.tagEditable,
    sections: state.sections,
    referenceTypes: state.referenceTypes,
    saveState: state.saveState,
    config: state.config
  };
}

function mapDispatchToProps(dispatch) {
  return {
    tagActions: bindActionCreators(Object.assign({}, getTag, updateTag, saveTag, deleteTag), dispatch),
    sectionActions: bindActionCreators(Object.assign({}, getSections), dispatch),
    referenceTypeActions: bindActionCreators(Object.assign({}, getReferenceTypes), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(TagDisplay);
