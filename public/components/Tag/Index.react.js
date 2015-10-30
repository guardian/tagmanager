import React from 'react';
import TagEdit from './TagEdit.react';
import SaveButton from '../utils/SaveButton.react';

class TagIndex extends React.Component {

    constructor(props) {
        super(props);

        this.isTagDirty = this.isTagDirty.bind(this);
    }

    componentDidMount() {
      if (!this.props.tag) {
        this.props.tagActions.getTag(this.props.routeParams.tagId);
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

    render () {
      if (!this.props.tag) {
        return (
          <div>Fetching Tag</div>
        );
      }

      return (
        <div className="tag">
          <div className="tag__columns-wrapper">
            <div className="tag__column--sidebar">
              <TagEdit tag={this.props.tag} updateTag={this.props.tagActions.updateTag} />
            </div>
            <div className="tag__column">
              Column two.
            </div>
            <div className="tag__column">
              Column three.
            </div>
          </div>
          {this.isTagDirty() ? <SaveButton onSaveClick={this.saveTag.bind(this)} onResetClick={this.resetTag.bind(this)}/> : false}
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

function mapStateToProps(state) {
  return {
    tag: state.tag,
    saveState: state.saveState
  };
}

function mapDispatchToProps(dispatch) {
  return { tagActions: bindActionCreators(Object.assign({}, getTag, updateTag, saveTag), dispatch) };
}

export default connect(mapStateToProps, mapDispatchToProps)(TagIndex);