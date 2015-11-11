import React from 'react';
import TagEdit from '../TagEdit/TagEdit.react';
import TypeSelect from '../utils/TypeSelect.react';
import SaveButton from '../utils/SaveButton.react';
import TagValidationErrors from './TagValidation.react';
import {validateTag} from '../../util/validateTag';
import {creatableTags} from '../../constants/tagTypes.js';

const BLANK_TAG = {
  externalReferences: [],
  hidden: false,
  legallySensitive: false,
  pageId: 123456789,
  path: '/this/should/be/set/serverside',
  parents: []
};

class TagCreate extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          newTag: BLANK_TAG
        };
    }

    componentDidMount() {
      if (!this.props.sections || !this.props.sections.length) {
        this.props.sectionActions.getSections();
      }
    }

    saveTag() {
      this.props.tagActions.createTag(this.state.newTag);
    }

    updateTag(tag) {
      this.setState({
        newTag: tag
      });
    }

    resetTag() {
      this.setState({
        newTag: BLANK_TAG
      });
    }

    isTagValid() {
      return !validateTag(this.state.newTag).length;
    }

    onUpdateType(e) {
      this.setState({
        newTag: Object.assign({}, this.state.newTag, {type: e.target.value})
      });
    }

    render () {
      return (
        <div className="tag">
          <div className="tag__columns-wrapper">
            <div className="tag__column--sidebar">
              <div className="tag-edit__input-group">
                <label className="tag-edit__input-group__header">Tag Type</label>
                <TypeSelect selectedType={this.state.newTag.type} onChange={this.onUpdateType.bind(this)}/>
              </div>
              <TagEdit tag={this.state.newTag} sections={this.props.sections} updateTag={this.updateTag.bind(this)} />
              <TagValidationErrors validations={validateTag(this.state.newTag)} />
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
import * as getSections from '../../actions/getSections';

function mapStateToProps(state) {
  return {
    sections: state.sections
  };
}

function mapDispatchToProps(dispatch) {
  return {
    tagActions: bindActionCreators(Object.assign({}, createTag), dispatch),
    sectionActions: bindActionCreators(Object.assign({}, getSections), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(TagCreate);
