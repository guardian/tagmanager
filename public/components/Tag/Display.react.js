import React from 'react';
import TagEdit from '../TagEdit/TagEdit.react';
import TagContext from '../TagContext/TagContext.react';
import SaveButton from '../utils/SaveButton.react';
import TypeSelect from '../utils/TypeSelect.react';
import TagValidationErrors from './TagValidation.react';
import {validateTag} from '../../util/validateTag';
import CapiStats from '../CapiStats/CapiStats.react';

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

    componentWillReceiveProps(props) {
      if (props.tag.id !== parseInt(props.routeParams.tagId, 10)) {
        props.tagActions.getTag(parseInt(props.routeParams.tagId, 10));
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
              <TagEdit tag={this.props.tag} sections={this.props.sections} updateTag={this.props.tagActions.updateTag} pathLocked={true}/>
              <TagValidationErrors validations={validateTag(this.props.tag)} />
            </div>
            <div className="tag__column">
              <TagContext tag={this.props.tag} updateTag={this.props.tagActions.updateTag}/>
            </div>
            <div className="tag__column">
              <CapiStats tag={this.props.tag} config={this.props.config} />
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
import * as getTag from '../../actions/TagActions/getTag';
import * as updateTag from '../../actions/TagActions/updateTag';
import * as saveTag from '../../actions/TagActions/saveTag';
import * as getSections from '../../actions/SectionsActions/getSections';

function mapStateToProps(state) {
  return {
    tag: state.tag,
    sections: state.sections,
    saveState: state.saveState,
    config: state.config
  };
}

function mapDispatchToProps(dispatch) {
  return {
    tagActions: bindActionCreators(Object.assign({}, getTag, updateTag, saveTag), dispatch),
    sectionActions: bindActionCreators(Object.assign({}, getSections), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(TagDisplay);
