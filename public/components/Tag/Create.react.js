import React from 'react';
import TagEdit from './TagEdit.react';
import SaveButton from '../utils/SaveButton.react';

const BLANK_TAG = {
  externalReferences: [],
  hidden: false,
  legallySensitive: false,
  pageId: 123456789,
  parents: [],
  path: 'PATHNOTPOPULATED',
  type: 'TYPENOTPOPULATED'
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

      //Redirect to page
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

    render () {
      return (
        <div className="tag">
          <div className="tag__columns-wrapper">
            <div className="tag__column--sidebar">
              <TagEdit tag={this.state.newTag} sections={this.props.sections} updateTag={this.updateTag.bind(this)} />
            </div>
            <div className="tag__column">
              Column 2
            </div>
            <div className="tag__column">
              Column 3
            </div>
          </div>
          <SaveButton onSaveClick={this.saveTag.bind(this)} onResetClick={this.resetTag.bind(this)}/>
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
