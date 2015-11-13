import React from 'react';
import TagEdit from '../TagEdit/TagEdit.react';
import TypeSelect from '../utils/TypeSelect.react';
import SaveButton from '../utils/SaveButton.react';
import TagValidationErrors from './TagValidation.react';
import tagManagerApi from '../../util/tagManagerApi';
import {validateTag} from '../../util/validateTag';
import {creatableTags} from '../../constants/tagTypes.js';

class TagCreate extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          pathInUse: false
        };

    }

    componentDidMount() {

      this.props.tagActions.populateEmptyTag();

      if (!this.props.sections || !this.props.sections.length) {
        this.props.sectionActions.getSections();
      }
    }

    saveTag() {
      this.props.tagActions.createTag(this.props.tag);
    }

    sectionChanged(updated) {
      return updated.section !== this.props.tag.section;
    }

    slugChanged(updated) {
      return updated.slug !== this.props.tag.slug;
    }

    updateTag(tag) {
      const shouldCheckPath = tag.slug && (this.sectionChanged(tag) || this.slugChanged(tag));

      this.props.tagActions.updateTag(tag);

      if (shouldCheckPath) {
        this.checkPathInUse(tag);
      }
    }

    resetTag() {
      this.props.tagActions.populateEmptyTag();
    }

    isTagValid() {
      return !validateTag(this.props.tag).length && !this.state.pathInUse;
    }

    generateValidationErrors() {
      const validationErrors = validateTag(this.props.tag)

      if (this.state.pathInUse) {
        validationErrors.push({
          fieldName: 'slug',
          message: 'Path is already in use'
        });
      }

      return validationErrors;
    }

    checkPathInUse(tag) {
      tagManagerApi.checkPathInUse(tag.type, tag.slug, tag.section)
        .then(res => this.setState({pathInUse: res.inUse}))
        .fail(error => this.setState({pathInUse: true}));
    }

    onUpdateType(e) {
      this.props.tagActions.updateTag(Object.assign({}, this.props.tag, {type: e.target.value}));
    }

    render () {

      if (!this.props.tag) {
        return false;
      }

      return (
        <div className="tag">
          <div className="tag__columns-wrapper">
            <div className="tag__column--sidebar">
              <div className="tag-edit__input-group">
                <label className="tag-edit__input-group__header">Tag Type</label>
                <TypeSelect selectedType={this.props.tag.type} onChange={this.onUpdateType.bind(this)}/>
              </div>
              <TagEdit tag={this.props.tag} sections={this.props.sections} updateTag={this.updateTag.bind(this)} />
              <TagValidationErrors validations={this.generateValidationErrors()} />
            </div>
            <div className="tag__column">
              Column 2
            </div>
            <div className="tag__column">
              Column 3
            </div>
          </div>
          <SaveButton isHidden={!this.isTagValid()} onSaveClick={this.saveTag.bind(this)} onResetClick={this.resetTag.bind(this)}/>
        </div>
      );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as createTag from '../../actions/createTag';
import * as updateTag from '../../actions/updateTag';
import * as getSections from '../../actions/getSections';

function mapStateToProps(state) {
  return {
    sections: state.sections,
    tag: state.tag
  };
}

function mapDispatchToProps(dispatch) {
  return {
    tagActions: bindActionCreators(Object.assign({}, updateTag, createTag), dispatch),
    sectionActions: bindActionCreators(Object.assign({}, getSections), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(TagCreate);
